package idl.helper;

public class IDLBool extends IDLBoolArray {

    public static final IDLBool NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLBool native_new() {
        return new IDLBool((byte) 1, (char) 1);
    }

    private IDLBool(byte b, char c) {
        super(b, c);
    }

    public IDLBool() {
        super(1);
    }

    public IDLBool(boolean value) {
        this();
        set(value);
    }

    public IDLBool set(boolean value) {
        setValue(0, value);
        return this;
    }

    public boolean getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}