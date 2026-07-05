package runtime.helper;

public class NativeDouble2 extends NativeDoubleArray {

    public static final NativeDouble2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeDouble2 native_new() {
        return new NativeDouble2((byte) 1, (char) 1);
    }

    protected NativeDouble2(byte b, char c) {
    }

    public NativeDouble2() {
    }
}
