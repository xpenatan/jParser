package idl.helper;

public class IDLFloat3 extends IDLFloatArray {

    public static final IDLFloat3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat3 native_new() {
        return new IDLFloat3((byte) 1, (char) 1);
    }

    protected IDLFloat3(byte b, char c) {
    }

    public IDLFloat3() {
    }
}