package idl.helper;

public class IDLLong3 extends IDLLongArray {

    public static final IDLLong3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong3 native_new() {
        return new IDLLong3((byte) 1, (char) 1);
    }

    protected IDLLong3(byte b, char c) {
    }

    public IDLLong3() {
    }
}