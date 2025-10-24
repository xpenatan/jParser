package idl.helper;

public class IDLDouble3 extends IDLDoubleArray {

    public static final IDLDouble3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble3 native_new() {
        return new IDLDouble3((byte) 1, (char) 1);
    }

    protected IDLDouble3(byte b, char c) {
    }

    public IDLDouble3() {
    }
}