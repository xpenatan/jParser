package runtime.helper;

public class NativeDouble4 extends NativeDoubleArray {

    public static final NativeDouble4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeDouble4 native_new() {
        return new NativeDouble4((byte) 1, (char) 1);
    }

    protected NativeDouble4(byte b, char c) {
    }

    public NativeDouble4() {
    }
}
