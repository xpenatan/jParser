package runtime.helper;

public class NativeFloat2 extends NativeFloatArray {

    public static final NativeFloat2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeFloat2 native_new() {
        return new NativeFloat2((byte) 1, (char) 1);
    }

    protected NativeFloat2(byte b, char c) {
    }

    public NativeFloat2() {
    }
}
