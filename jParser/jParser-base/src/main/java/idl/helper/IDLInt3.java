package idl.helper;

public class IDLInt3 extends IDLIntArray {

    public static final IDLInt3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt3 native_new() {
        return new IDLInt3((byte) 1, (char) 1);
    }

    protected IDLInt3(byte b, char c) {
    }

    public IDLInt3() {
    }

    public native void set(int value);
    public native int getValue();
}