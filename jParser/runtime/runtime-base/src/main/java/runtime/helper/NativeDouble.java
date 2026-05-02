package runtime.helper;

public class NativeDouble extends NativeDoubleArray {

    public static final NativeDouble NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeDouble native_new() {
        return new NativeDouble((byte) 1, (char) 1);
    }

    protected NativeDouble(byte b, char c) {
    }

    public NativeDouble() {
    }

    public native void set(double value);
    public native double getValue();
}
