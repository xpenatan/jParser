package idl.helper;

public class IDLLong2 extends IDLLongArray {

    public static final IDLLong2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong2 native_new() {
        return new IDLLong2((byte) 1, (char) 1);
    }

    protected IDLLong2(byte b, char c) {
    }

    public IDLLong2() {
    }
}