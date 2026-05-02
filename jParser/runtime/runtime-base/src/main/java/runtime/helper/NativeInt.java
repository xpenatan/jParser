package runtime.helper;

public class NativeInt extends NativeIntArray {

    public static final NativeInt NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeInt native_new() {
        return new NativeInt((byte) 1, (char) 1);
    }

    protected NativeInt(byte b, char c) {
    }

    public NativeInt() {
    }

    public native void set(int value);
    public native int getValue();
}
