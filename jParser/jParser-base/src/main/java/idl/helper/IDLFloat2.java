package idl.helper;

public class IDLFloat2 extends IDLFloatArray {

    public static final IDLFloat2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat2 native_new() {
        return new IDLFloat2((byte) 1, (char) 1);
    }

    protected IDLFloat2(byte b, char c) {
    }

    public IDLFloat2() {
    }
}