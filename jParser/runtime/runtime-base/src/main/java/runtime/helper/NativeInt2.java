package runtime.helper;

public class NativeInt2 extends NativeIntArray {

    public static final NativeInt2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeInt2 native_new() {
        return new NativeInt2((byte) 1, (char) 1);
    }

    protected NativeInt2(byte b, char c) {
    }

    public NativeInt2() {
    }
}
