package idl.helper;

public class IDLLong4 extends IDLLongArray {

    public static final IDLLong4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong4 native_new() {
        return new IDLLong4((byte) 1, (char) 1);
    }

    protected IDLLong4(byte b, char c) {
    }

    public IDLLong4() {
    }
}