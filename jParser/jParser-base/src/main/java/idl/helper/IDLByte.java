package idl.helper;

public class IDLByte extends IDLPointer {

    public static final IDLByte NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLByte native_new() {
        return new IDLByte((byte) 1, (char) 1);
    }

    protected IDLByte(byte b, char c) {
    }

    public IDLByte() {
    }

    public native void setValue(byte value);
    public native byte getValue();
}