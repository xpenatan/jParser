package runtime.helper;

public class NativeFloat4 extends NativeFloatArray {

    public static final NativeFloat4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeFloat4 native_new() {
        return new NativeFloat4((byte) 1, (char) 1);
    }

    protected NativeFloat4(byte b, char c) {
    }

    public NativeFloat4() {
    }
}
