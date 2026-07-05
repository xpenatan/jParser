package runtime.helper;

public class NativeLong extends NativeLongArray {

    public static final NativeLong NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeLong native_new() {
        return new NativeLong((byte) 1, (char) 1);
    }

    protected NativeLong(byte b, char c) {
    }

    public NativeLong() {
    }

    public native void set(long value);
    public native long getValue();
}
