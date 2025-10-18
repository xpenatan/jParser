package idl.helper;

public class IDLLong extends IDLArrayLong {

    public static final IDLLong NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong native_new() {
        return new IDLLong((byte) 1, (char) 1);
    }

    protected IDLLong(byte b, char c) {
    }

    public IDLLong() {
    }

    public native void setValue(long value);
    public native long getValue();
}