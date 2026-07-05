package runtime.helper;

public class NativeLong2 extends NativeLongArray {

    public static final NativeLong2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeLong2 native_new() {
        return new NativeLong2((byte) 1, (char) 1);
    }

    protected NativeLong2(byte b, char c) {
    }

    public NativeLong2() {
    }
}
