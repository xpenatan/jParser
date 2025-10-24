package idl.helper;

public class IDLInt2 extends IDLIntArray {

    public static final IDLInt2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt2 native_new() {
        return new IDLInt2((byte) 1, (char) 1);
    }

    protected IDLInt2(byte b, char c) {
    }

    public IDLInt2() {
    }
}