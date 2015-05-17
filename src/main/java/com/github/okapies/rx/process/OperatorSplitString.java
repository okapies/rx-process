package com.github.okapies.rx.process;

import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;

/**
 * <p>Split a stream of strings based on a regex pattern, and rechunk them.</p>
 *
 * <p>This operator instance itself is immutable, but a subscriber produced by
 * the operator has its internal state.</p>
 */
class OperatorSplitString implements Observable.Operator<String, String> {

    private final Pattern pattern; // Pattern is immutable and thread-safe.

    public OperatorSplitString(Pattern pattern) {
        this.pattern = pattern;
    }

    public OperatorSplitString(String regex) {
        this(Pattern.compile(regex));
    }

    public Pattern pattern() {
        return this.pattern;
    }

    public OperatorSplitString pattern(Pattern pattern) {
        return new OperatorSplitString(pattern);
    }

    public OperatorSplitString pattern(String regex) {
        return new OperatorSplitString(regex);
    }

    @Override
    public Subscriber<? super String> call(Subscriber<? super String> op) {
        return new Subscriber<String>() {

            private String last = null;

            @Override
            public void onNext(String str) {
                if (str == null) {
                    return;
                }

                final String[] subs = pattern.split(str, -1);
                final int count = subs.length;

                if (last != null) {
                    subs[0] = last + subs[0];
                }
                for (int i = 0; i < count - 1; i++) {
                    if (!op.isUnsubscribed()) {
                        op.onNext(subs[i]);
                    }
                }
                last = subs[count - 1];
            }

            @Override
            public void onCompleted() {
                if (!op.isUnsubscribed()) {
                    if (last != null) {
                        op.onNext(last);
                    }
                    op.onCompleted();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!op.isUnsubscribed()) {
                    if (last != null) {
                        op.onNext(last);
                    }
                    op.onError(t);
                }
            }

        };
    }

}
