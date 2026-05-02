package runtime.helper;

import com.github.xpenatan.jParser.api.NativeObject;

public class NativeTemp extends NativeObject {

    public static final NativeTemp NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeTemp native_new() {
        return new NativeTemp((byte) 1, (char) 1);
    }

    protected NativeTemp(byte b, char c) {
    }

    public NativeTemp() {
    }
}
