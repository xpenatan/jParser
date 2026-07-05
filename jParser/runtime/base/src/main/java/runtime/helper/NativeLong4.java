package runtime.helper;

public class NativeLong4 extends NativeLongArray {

    public static final NativeLong4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeLong4 native_new() {
        return new NativeLong4((byte) 1, (char) 1);
    }

    protected NativeLong4(byte b, char c) {
    }

    public NativeLong4() {
    }
}
