package com.github.xpenatan.jParser.api;

/**
 * @author xpenatan
 */
public interface NativeEnum<T extends NativeEnum<T>> {
    int getValue();
    T setValue(int value);
    T getCustom();

    default boolean contains(NativeEnum<?> flag) {
        if (flag == null) {
            throw new IllegalArgumentException("Flag cannot be null");
        }
        return (this.getValue() & flag.getValue()) != 0;
    }

    default T or(NativeEnum<?> other) {
        return combine(this, other);
    }

    default T and(NativeEnum<?> other) {
        return and(this, other);
    }

    default boolean isEqual(T other) {
        return getValue() == other.getValue();
    }

    default boolean isNotEqual(T other) {
        return getValue() != other.getValue();
    }

    static <T extends NativeEnum<T>> T combine(NativeEnum<T> flag1, NativeEnum<?> flag2) {
        if (flag1 == null || flag2 == null) {
            throw new IllegalArgumentException("Flags cannot be null");
        }
        T custom = flag1.getCustom();
        int result = flag1.getValue() | flag2.getValue();
        custom.setValue(result);
        return custom;
    }

    static <T extends NativeEnum<T>> T and(NativeEnum<T> flag1, NativeEnum<?> flag2) {
        if (flag1 == null || flag2 == null) {
            throw new IllegalArgumentException("Flags cannot be null");
        }
        T custom = flag1.getCustom();
        int result = flag1.getValue() & flag2.getValue();
        custom.setValue(result);
        return custom;
    }
}