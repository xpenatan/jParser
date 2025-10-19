package idl.helper;

public class IDLInt extends IDLIntArray {

    public static final IDLInt NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt native_new() {
        return new IDLInt((byte) 1, (char) 1);
    }

    protected IDLInt(byte b, char c) {
    }

    public IDLInt() {
    }

    public native void set(int value);
    public native int getValue();
}