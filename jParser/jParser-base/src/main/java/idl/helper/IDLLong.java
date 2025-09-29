package idl.helper;

public class IDLLong extends IDLLongArray {

    public static final IDLLong NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong native_new() {
        return new IDLLong((byte) 1, (char) 1);
    }

    private IDLLong(byte b, char c) {
        super(b, c);
    }

    public IDLLong() {
        super(1);
    }

    public IDLLong(int value) {
        this();
        set(value);
    }

    public IDLLong set(long value) {
        setValue(0, value);
        return this;
    }

    public long getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}