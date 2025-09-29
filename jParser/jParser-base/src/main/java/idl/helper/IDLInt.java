package idl.helper;

public class IDLInt extends IDLIntArray {

    public static final IDLInt NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt native_new() {
        return new IDLInt((byte) 1, (char) 1);
    }

    private IDLInt(byte b, char c) {
        super(b, c);
    }

    public IDLInt() {
        super(1);
    }

    public IDLInt(int value) {
        this();
        set(value);
    }

    public IDLInt set(int value) {
        setValue(0, value);
        return this;
    }

    public int getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}