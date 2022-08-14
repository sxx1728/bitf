package com.bitfye.common.base.util;

import java.util.function.Function;

/**
 * @since 2019-12-11
 */
public class NullableUtils {

    private NullableUtils() {

    }

    public static <T> T defaultValue(T input, T defaultValue) {
        return input != null ? input : defaultValue;
    }

    public static <T, R> R apply(T input, Function<T, R> function) {
        if (input == null) {
            return null;
        }
        return function.apply(input);
    }

    public static <T, R> R apply(T input, Function<T, R> function, R defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        return function.apply(input);
    }

    public static <T, R> Function<T, R> wrap(Function<T, R> function) {
        return input -> input == null ? null : function.apply(input);
    }
}
