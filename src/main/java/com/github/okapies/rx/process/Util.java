package com.github.okapies.rx.process;

import rx.Observable;
import rx.Subscriber;

class Util {

    public static <R, U, T> Observable.Operator<R, T> compose(
            Observable.Operator<R, U> f,
            Observable.Operator<U, T> g) {
        return new Observable.Operator<R, T>() {
            public Subscriber<? super T> call(Subscriber<? super R> op) {
                return g.call(f.call(op));
            }
        };
    }

}
