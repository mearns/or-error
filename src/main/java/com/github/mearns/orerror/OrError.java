package com.github.mearns.orerror;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class OrError<V, E extends Throwable> {

    private OrError() {}

    public abstract V get() throws E;

    public abstract Optional<V> getValue();

    public abstract Optional<E> getError();

    public abstract <R> OrError<R, E> map(Function<V, R> func);

    public abstract <R> OrError<R, E> map(Class<E> eCls, CheckedFunction<V, R, E> checkedFunc);

    public static <I, R, E extends Throwable> Function<I, OrError<R, E>> wrap(Class<E> eCls, CheckedFunction<I, R, E> func) {
        return (I input) -> mapValue(input, eCls, func);
    }

    public static <V, E extends Throwable> OrError<List<V>, E> unpack(List<OrError<V, ? extends E>> packed){
        List<V> unpacked = new ArrayList<>(packed.size());
        for (OrError<V, ? extends E> item : packed) {
            Optional<? extends E> error = item.getError();
            if (error.isPresent()) {
                return caught(error.get());
            }
            unpacked.add(item.getValue().get());
        }
        return of(unpacked);
    }

    private static <V, R, E extends Throwable> OrError<R, E> mapValue (V value, Class<? super E> eCls, CheckedFunction<V, R, E> checkedFunc) {
        try {
            return of(checkedFunc.apply(value));
        } catch (Throwable e) {
            if (eCls.isInstance(e)) {
                return caught((E)e);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw new RuntimeException("CheckedFunction threw unexpected checked exception", e);
        }
    }

    public static <V, E extends Throwable> OrError<V, E> of(final V value) {
        return new OrError<V, E>() {
            @Override
            public V get () throws E {
                return value;
            }

            @Override
            public Optional<V> getValue () {
                return Optional.of(value);
            }

            @Override
            public Optional<E> getError () {
                return Optional.empty();
            }

            @Override
            public <R> OrError<R, E> map (Function<V, R> func) {
                return of(func.apply(value));
            }

            @Override
            public <R> OrError<R, E> map (Class<E> eCls, CheckedFunction<V, R, E> checkedFunc) {
                return mapValue(value, eCls, checkedFunc);
            }
        };
    }

    public static <V, E extends Throwable> OrError<V, E> caught(final E error) {
        return new OrError<V, E>() {
            @Override
            public V get () throws E {
                throw error;
            }

            @Override
            public Optional<V> getValue () {
                return Optional.empty();
            }

            @Override
            public Optional<E> getError () {
                return Optional.of(error);
            }

            @Override
            public <R> OrError<R, E> map (Function<V, R> func) {
                return caught(error);
            }

            @Override
            public <R> OrError<R, E> map (Class<E> eCls, CheckedFunction<V, R, E> checkedFunc) {
                return caught(error);
            }
        };
    }
}
