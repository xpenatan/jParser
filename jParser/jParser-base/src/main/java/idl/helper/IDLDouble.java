package idl.helper;

public class IDLDouble extends IDLDoubleArray {

    public static final IDLDouble NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble native_new() {
        return new IDLDouble((byte) 1, (char) 1);
    }

    private IDLDouble(byte b, char c) {
        super(b, c);
    }

    public IDLDouble() {
        super(1);
    }

    public IDLDouble(double value) {
        this();
        set(value);
    }

    public IDLDouble set(double value) {
        setValue(0, value);
        return this;
    }

    public double getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}