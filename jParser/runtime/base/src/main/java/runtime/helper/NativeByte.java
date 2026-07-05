package runtime.helper;

public class NativeByte extends NativeByteArray {

    public static final NativeByte NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeByte native_new() {
        return new NativeByte((byte) 1, (char) 1);
    }

    protected NativeByte(byte b, char c) {
    }

    public NativeByte() {
    }

    public native void set(byte value);
    public native byte getValue();
}
