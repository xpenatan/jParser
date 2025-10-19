package idl.helper;

public class IDLFloat extends IDLFloatArray {

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

    public native void set(float value);
    public native float getValue();
}