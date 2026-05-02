package runtime.helper;

public class NativeFloat3 extends NativeFloatArray {

    public static final NativeFloat3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeFloat3 native_new() {
        return new NativeFloat3((byte) 1, (char) 1);
    }

    protected NativeFloat3(byte b, char c) {
    }

    public NativeFloat3() {
    }
}
