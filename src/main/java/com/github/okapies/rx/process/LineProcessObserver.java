package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;
import rx.subjects.Subject;

public class LineProcessObserver implements ProcessObserver<String> {

    private final StringProcessObserver underlying;

    private final boolean ignoreLF;

    private LineProcessObserver(StringProcessObserver underlying, boolean ignoreLF) {
        this.underlying = underlying;
        this.ignoreLF = ignoreLF;
    }

    LineProcessObserver(
            Observer<String> stdout,
            Observer<String> stderr,
            Charset charset,
            boolean ignoreLF) {
        this(new StringProcessObserver(stdout, stderr, charset), ignoreLF);
    }

    public LineProcessObserver() {
        this(new StringProcessObserver(), false);
    }

    public Observer<String> stdout() {
        return underlying.stdout();
    }

    public LineProcessObserver stdout(Observer<String> o) {
        return new LineProcessObserver(o, underlying.stderr(), underlying.charset(), this.ignoreLF);
    }

    public Observer<String> stderr() {
        return underlying.stderr();
    }

    public LineProcessObserver stderr(Observer<String> o) {
        return new LineProcessObserver(underlying.stdout(), o, underlying.charset(), this.ignoreLF);
    }

    public LineProcessObserver charset(Charset charset) {
        return new LineProcessObserver(underlying.stdout(), underlying.stderr(), charset, this.ignoreLF);
    }

    public boolean ignoreLF() {
        return this.ignoreLF;
    }

    public LineProcessObserver ignoreLF(boolean ignoreLF) {
        return new LineProcessObserver(
                underlying.stdout(), underlying.stderr(), underlying.charset(), ignoreLF);
    }

    public Observable<String> serialize(Observable<ByteBuffer> o) {
        final StringBuilder remaining = new StringBuilder();
        final Subject<String, String> last = AsyncSubject.create();

        return underlying.serialize(o).doOnCompleted(new Action0() {
            @Override
            public void call() {
                if (remaining.length() > 0) {
                    last.onNext("");
                }
                last.onCompleted();
            }
        }).concatWith(last).flatMap(new Func1<String, Observable<String>>() {
            public Observable<String> call(String str) {
                final List<String> lines = Arrays.asList(str.split("(\r|\n|\r\n)", -1));
                final int count = lines.size();

                if (count > 0) {
                    if (remaining.length() > 0) {
                        remaining.append(lines.get(0));
                        lines.set(0, remaining.toString());
                        remaining.setLength(0);
                    }
                }
                if (count > 1) {
                    remaining.append(lines.get(count - 1));
                    return Observable.from(lines.subList(0, count - 1));
                } else {
                    return Observable.from(lines);
                }
            }
        });
    }

    public ByteBuffer deserialize(String t) {
        return underlying.deserialize(t);
    }

}
