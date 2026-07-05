package runtime.helper;

public class NativeInt4 extends NativeIntArray {

    public static final NativeInt4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeInt4 native_new() {
        return new NativeInt4((byte) 1, (char) 1);
    }

    protected NativeInt4(byte b, char c) {
    }

    public NativeInt4() {
    }
}
