package idl.helper;

public class IDLDouble extends IDLDoubleArray {

    public static final IDLDouble NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble native_new() {
        return new IDLDouble((byte) 1, (char) 1);
    }

    protected IDLDouble(byte b, char c) {
    }

    public IDLDouble() {
    }

    public native void setValue(double value);
    public native double getValue();
}