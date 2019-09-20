package com.github.mearns.orerror;

import java.util.function.Function;

public interface CheckedFunction<I, R, E extends Throwable> {
    R apply(I input) throws E;

    static <I, R, E extends RuntimeException> CheckedFunction<I, R, E> from(
            Function<I, R> func
    ) {
        return func::apply;
    }
}
