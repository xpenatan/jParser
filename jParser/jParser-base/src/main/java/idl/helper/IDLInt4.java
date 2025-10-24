package idl.helper;

public class IDLInt4 extends IDLIntArray {

    public static final IDLInt4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt4 native_new() {
        return new IDLInt4((byte) 1, (char) 1);
    }

    protected IDLInt4(byte b, char c) {
    }

    public IDLInt4() {
    }

    public native void set(int value);
    public native int getValue();
}