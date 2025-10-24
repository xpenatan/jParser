package idl.helper;

public class IDLDouble2 extends IDLDoubleArray {

    public static final IDLDouble2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble2 native_new() {
        return new IDLDouble2((byte) 1, (char) 1);
    }

    protected IDLDouble2(byte b, char c) {
    }

    public IDLDouble2() {
    }

    public native void set(double value);
    public native double getValue();
}