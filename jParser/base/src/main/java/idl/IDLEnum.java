package idl;

/**
 * @author xpenatan
 */
public interface IDLEnum<T extends IDLEnum<T>> {
    int getValue();
    void setValue(int value);
    T getCustom();

    default boolean contains(IDLEnum flag) {
        if (flag == null) {
            throw new IllegalArgumentException("Flag cannot be null");
        }
        return (this.getValue() & flag.getValue()) != 0;
    }

    default T or(T other) {
        return combine(this, other);
    }

    default T and(T other) {
        return and(this, other);
    }

    static <T extends IDLEnum<T>> T combine(IDLEnum<T> flag1, T flag2) {
        if (flag1 == null || flag2 == null) {
            throw new IllegalArgumentException("Flags cannot be null");
        }
        T custom = flag1.getCustom();
        synchronized (custom) {
            int result = flag1.getValue() | flag2.getValue();
            custom.setValue(result);
            return custom;
        }
    }

    static <T extends IDLEnum<T>> T and(IDLEnum<T> flag1, T flag2) {
        if (flag1 == null || flag2 == null) {
            throw new IllegalArgumentException("Flags cannot be null");
        }
        T custom = flag1.getCustom();
        synchronized (custom) {
            int result = flag1.getValue() & flag2.getValue();
            custom.setValue(result);
            return custom;
        }
    }
}