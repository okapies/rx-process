package com.github.okapies.rx.process;

import rx.Observable;
import rx.Observer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.github.okapies.rx.process.Util.compose;

public class ByLineProcessObserver extends AbstractProcessObserver<String> {

    private final Charset charset;

    ByLineProcessObserver(
            Observer<String> stdout,
            Observer<String> stderr,
            Charset charset) {
        super(stdout, stderr);
        this.charset = charset;
    }

    public ByLineProcessObserver() {
        super(null, null);
        this.charset = Charset.defaultCharset();
    }

    public ByLineProcessObserver stdout(Observer<String> o) {
        return new ByLineProcessObserver(o, this.stderr, this.charset);
    }

    public ByLineProcessObserver stderr(Observer<String> o) {
        return new ByLineProcessObserver(this.stdout, o, this.charset);
    }

    public Charset charset() {
        return this.charset;
    }

    public ByLineProcessObserver charset(Charset charset) {
        return new ByLineProcessObserver(this.stdout, this.stderr, charset);
    }

    @Override
    public Observable.Operator<String, ByteBuffer> decoder() {
        return compose(new OperatorSplitString("\r?\n"), new OperatorByteBufferToString(charset));
    }

}
