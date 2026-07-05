package runtime.helper;

public class NativeInt3 extends NativeIntArray {

    public static final NativeInt3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeInt3 native_new() {
        return new NativeInt3((byte) 1, (char) 1);
    }

    protected NativeInt3(byte b, char c) {
    }

    public NativeInt3() {
    }
}
