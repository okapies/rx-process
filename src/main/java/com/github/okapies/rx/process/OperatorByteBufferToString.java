package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

import rx.Observable;
import rx.Subscriber;

/**
 * <p>Decodes a stream of byte buffers into a stream of strings. It can handle
 * when a multi-byte character spans two or more chunks. Unlike StringObservable
 * in rxjava-string, it is optimized for processing <i>direct</i> byte buffers.</p>
 *
 * <p>This operator instance itself is immutable, but a subscriber produced by
 * the operator has its internal state.</p>
 */
class OperatorByteBufferToString implements Observable.Operator<String, ByteBuffer> {

    private static final int INITIAL_LAST_BUFFER_SIZE = 8192;

    private final Charset charset;

    private final CodingErrorAction malformedInputAction;

    private final CodingErrorAction unmappableCharacterAction;

    private final String replacement;

    public OperatorByteBufferToString(Charset charset) {
        this(charset, CodingErrorAction.REPLACE, CodingErrorAction.REPLACE, "\uFFFD");
    }

    public OperatorByteBufferToString(
            Charset charset,
            CodingErrorAction malformedInputAction,
            CodingErrorAction unmappableCharacterAction,
            String replacement) {
        this.charset = charset;
        this.malformedInputAction = malformedInputAction;
        this.unmappableCharacterAction = unmappableCharacterAction;
        this.replacement = replacement;
    }

    public Charset charset() {
        return this.charset;
    }

    public OperatorByteBufferToString charset(Charset charset) {
        return new OperatorByteBufferToString(
                charset,
                this.malformedInputAction,
                this.unmappableCharacterAction,
                this.replacement);
    }

    public CodingErrorAction malformedInputAction() {
        return this.malformedInputAction;
    }

    public OperatorByteBufferToString malformedInputAction(CodingErrorAction action) {
        return new OperatorByteBufferToString(
                this.charset,
                action,
                this.unmappableCharacterAction,
                this.replacement);
    }

    public CodingErrorAction unmappableCharacterAction() {
        return this.unmappableCharacterAction;
    }

    public OperatorByteBufferToString unmappableCharacterAction(CodingErrorAction action) {
        return new OperatorByteBufferToString(
                this.charset,
                this.malformedInputAction,
                action,
                this.replacement);
    }

    public String replacement() {
        return this.replacement;
    }

    public OperatorByteBufferToString replacement(String replacement) {
        return new OperatorByteBufferToString(
                this.charset,
                this.malformedInputAction,
                this.unmappableCharacterAction,
                replacement);
    }

    @Override
    public Subscriber<? super ByteBuffer> call(Subscriber<? super String> op) {
        return new Subscriber<ByteBuffer>(op) {

            // CharsetDecoder maintains internal state.
            private final CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(malformedInputAction)
                    .onUnmappableCharacter(unmappableCharacterAction)
                    .replaceWith(replacement)
                    .reset();

            private ByteBuffer last = null;

            @Override
            public void onNext(ByteBuffer buf){
                process(buf, false);
            }

            @Override
            public void onCompleted() {
                if (process(null, true)) {
                    if (!op.isUnsubscribed()) {
                        op.onCompleted();
                    }
                }
            }

            @Override
            public void onError(Throwable t){
                if (process(null, true)) {
                    if (!op.isUnsubscribed()) {
                        op.onError(t);
                    }
                }
            }

            /**
             * Note: We should update internal state of this subscriber if
             * no Subscribers are interested in our items.
             */
            private boolean process(ByteBuffer next, boolean endOfInput){
                final int lastLen = last != null ? last.position() : 0;
                final boolean hasLast = lastLen > 0;
                final int nextLen = next != null ? next.remaining() : 0;
                final boolean hasNext = nextLen > 0;
                final int inLen = lastLen + nextLen;

                ByteBuffer in;
                if (!hasLast) {
                    if (hasNext) {
                        // directly utilize `next` as an input
                        in = next;
                    } else {
                        return true;
                    }
                } else { // `last` will not be null
                    // expand the last buffer if needed
                    // max size = (decoder.maxCharsPerByte() + NuProcess.BUFFER_CAPACITY)
                    if (last.remaining() - nextLen < 0) {
                        ByteBuffer newLast = ByteBuffer.allocateDirect(inLen);
                        last.flip();
                        newLast.put(last);
                        last = newLast;
                    }

                    // append bytes after the reminder bytes, and flip
                    if (hasNext) {
                        last.put(next);
                    }
                    last.flip();

                    // use the remaining buffer as input
                    in = last;
                }

                // cast to double to prevent losing low order bits
                int outCapacity = (int) Math.ceil(inLen * (double) decoder.maxCharsPerByte());
                char[] outChars = new char[outCapacity];
                CharBuffer out = CharBuffer.wrap(outChars);

                // decode the input
                CoderResult cr = decoder.decode(in, out, endOfInput);
                if (cr.isError()) {
                    try {
                        cr.throwException();
                    }
                    catch (CharacterCodingException e) {
                        if (!op.isUnsubscribed()) {
                            op.onError(e);
                        }
                        return false;
                    }
                }
                if (endOfInput) {
                    cr = decoder.flush(out);
                    if (cr.isError()) {
                        try {
                            cr.throwException();
                        }
                        catch (CharacterCodingException e) {
                            if (!op.isUnsubscribed()) {
                                op.onError(e);
                            }
                            return false;
                        }
                    }
                }

                // save the remainder bytes
                if (!hasLast) {
                    if (in.remaining() > 0) {
                        if (last == null) {
                            // initialize the last buffer
                            last = ByteBuffer.allocateDirect(INITIAL_LAST_BUFFER_SIZE);
                        }
                        last.clear();
                        last.put(in);
                    }
                } else { // `last` will not be null
                    last.compact();
                }

                int outLen = out.position();
                if (outLen > 0) {
                    if (!op.isUnsubscribed()) {
                        op.onNext(new String(outChars, 0, outLen));
                    }
                }

                return true;
            }

        };
    }

}
