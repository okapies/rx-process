package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;
import rx.subjects.Subject;

public class StringProcessObserver extends AbstractProcessObserver<String> {

    private final Charset charset;

    StringProcessObserver(Observer<String> stdout, Observer<String> stderr, Charset charset) {
        super(stdout, stderr);
        this.charset = charset;
    }

    public StringProcessObserver() {
        super(null, null);
        this.charset = Charset.defaultCharset();
    }

    public StringProcessObserver stdout(Observer<String> o) {
        return new StringProcessObserver(o, this.stderr, this.charset);
    }

    public StringProcessObserver stderr(Observer<String> o) {
        return new StringProcessObserver(this.stdout, o, this.charset);
    }

    public Charset charset() {
        return this.charset;
    }

    public StringProcessObserver charset(Charset charset) {
        return new StringProcessObserver(this.stdout, this.stderr, charset);
    }

    public Observable<String> serialize(Observable<ByteBuffer> o) {
        final ByteBufferToStringFunc f = new ByteBufferToStringFunc(charset);
        final Subject<ByteBuffer, ByteBuffer> last = AsyncSubject.create();

        // NOTE: `last` will be ignored when `o` triggers onError notification
        return o.doOnCompleted(new Action0() {
            public void call() {
                ByteBuffer remaining = f.remaining;
                if (remaining != null && remaining.remaining() > 0) {
                    // flush the remaining buffer
                    last.onNext(ByteBuffer.allocate(0));
                }
                last.onCompleted();
            }
        }).concatWith(last).map(f);
    }

    /**
     * It is stateful function to handle partial character bytes in each input buffer.
     */
    private static class ByteBufferToStringFunc implements Func1<ByteBuffer, String> {

        private static final int DEFAULT_REMAINING_BUFFER_SIZE = 8192;

        private final CharsetDecoder decoder;
        private ByteBuffer remaining = null;

        public ByteBufferToStringFunc(Charset charset) {
            decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }

        @Override
        public String call(ByteBuffer buf) {
            final int remainingLen = remaining != null ? remaining.position() : 0;
            final boolean hasRemaining = remainingLen > 0;
            final int bufLen = buf.remaining();
            final int inLen = remainingLen + bufLen;

            ByteBuffer inBuf;
            if (!hasRemaining) {
                // directly use `buf` as input
                inBuf = buf;
            } else { // `remaining` will not be null in this place
                // expand the remaining buffer if needed
                // max size = (decoder.maxCharsPerByte() + NuProcess.BUFFER_CAPACITY)
                if (remaining.remaining() - bufLen < 0) {
                    ByteBuffer newRemaining = ByteBuffer.allocateDirect(inLen);
                    remaining.flip();
                    newRemaining.put(remaining);
                    remaining = newRemaining;
                }

                // append bytes after the remaining bytes, and flip
                remaining.put(buf);
                remaining.flip();

                // use the remaining buffer as input
                inBuf = remaining;
            }

            // cast to double for preventing to lose low order bits
            int outLen = (int) Math.ceil(inLen * (double)decoder.maxCharsPerByte());
            char[] out = new char[outLen];
            CharBuffer outBuf = CharBuffer.wrap(out);

            // decode the input
            decoder.reset();
            CoderResult ret = decoder.decode(inBuf, outBuf, true);
            if (ret.isMalformed() || ret.isUnmappable()) {
                if (bufLen > 0) {
                    // remove the replacement character appended in the last position
                    int len = outBuf.position();
                    outBuf.position(len - decoder.replacement().length());
                } else {
                    // remain the replacement character and dispose remaining bytes
                    remaining.position(inBuf.limit());
                }
            }
            decoder.flush(outBuf);

            // save the remaining bytes
            if (!hasRemaining) {
                if (inBuf.remaining() > 0) {
                    if (remaining == null) {
                        // initialize remaining buffer
                        remaining = ByteBuffer.allocateDirect(DEFAULT_REMAINING_BUFFER_SIZE);
                    }
                    remaining.clear();
                    remaining.put(inBuf);
                }
            } else { // `remaining` will not be null
                remaining.compact();
            }

            return new String(out, 0, outBuf.position());
        }

    }

    public ByteBuffer deserialize(String t) {
        return ByteBuffer.wrap(t.getBytes(charset));
    }

}
