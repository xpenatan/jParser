package runtime.helper;

public class NativeBool extends NativeBoolArray {

    public static final NativeBool NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeBool native_new() {
        return new NativeBool((byte) 1, (char) 1);
    }

    protected NativeBool(byte b, char c) {
    }

    public NativeBool() {
    }

    public native void set(boolean value);
    public native boolean getValue();
}
