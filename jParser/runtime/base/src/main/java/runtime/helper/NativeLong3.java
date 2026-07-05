package runtime.helper;

public class NativeLong3 extends NativeLongArray {

    public static final NativeLong3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeLong3 native_new() {
        return new NativeLong3((byte) 1, (char) 1);
    }

    protected NativeLong3(byte b, char c) {
    }

    public NativeLong3() {
    }
}
