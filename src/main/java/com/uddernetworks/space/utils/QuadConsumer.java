// Copyright (c) 2014 Daniel S. Dickstein
// https://github.com/ddickstein/Java-Library/blob/master/java8/function/TriConsumer.java

package com.uddernetworks.space.utils;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<T, U, V, V2> {
    void accept(T t, U u, V v, V2 v2);

    default QuadConsumer<T, U, V, V2> andThen(QuadConsumer<? super T, ? super U, ? super V, ? super V2> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> {
            accept(a, b, c, d);
            after.accept(a, b, c, d);
        };
    }
}