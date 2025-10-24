package idl.helper;

public class IDLFloat4 extends IDLFloatArray {

    public static final IDLFloat4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat4 native_new() {
        return new IDLFloat4((byte) 1, (char) 1);
    }

    protected IDLFloat4(byte b, char c) {
    }

    public IDLFloat4() {
    }

    public native void set(float value);
    public native float getValue();
}