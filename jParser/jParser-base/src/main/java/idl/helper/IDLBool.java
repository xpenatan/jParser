package idl.helper;

public class IDLBool extends IDLBoolArray {

    public static final IDLBool NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLBool native_new() {
        return new IDLBool((byte) 1, (char) 1);
    }

    protected IDLBool(byte b, char c) {
    }

    public IDLBool() {
    }

    public native void setValue(boolean value);
    public native boolean getValue();
}