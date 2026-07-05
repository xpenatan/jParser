package runtime.helper;

public class NativeDouble3 extends NativeDoubleArray {

    public static final NativeDouble3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeDouble3 native_new() {
        return new NativeDouble3((byte) 1, (char) 1);
    }

    protected NativeDouble3(byte b, char c) {
    }

    public NativeDouble3() {
    }
}
