package idl.helper;

public class IDLFloat extends IDLPointer {

    public static final IDLFloat NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat native_new() {
        return new IDLFloat((byte) 1, (char) 1);
    }

    protected IDLFloat(byte b, char c) {
    }

    public IDLFloat() {
    }

    public native void setValue(float value);
    public native float getValue();
}