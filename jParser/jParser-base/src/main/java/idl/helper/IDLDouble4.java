package idl.helper;

public class IDLDouble4 extends IDLDoubleArray {

    public static final IDLDouble4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble4 native_new() {
        return new IDLDouble4((byte) 1, (char) 1);
    }

    protected IDLDouble4(byte b, char c) {
    }

    public IDLDouble4() {
    }
}