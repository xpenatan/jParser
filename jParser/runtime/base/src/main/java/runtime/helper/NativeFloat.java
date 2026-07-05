package runtime.helper;

public class NativeFloat extends NativeFloatArray {

    public static final NativeFloat NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeFloat native_new() {
        return new NativeFloat((byte) 1, (char) 1);
    }

    protected NativeFloat(byte b, char c) {
    }

    public NativeFloat() {
    }

    public native void set(float value);
    public native float getValue();
}
