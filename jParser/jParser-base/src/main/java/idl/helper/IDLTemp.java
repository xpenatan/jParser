package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;

public class IDLTemp extends IDLBase {

    public static final IDLTemp NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLTemp native_new() {
        return new IDLTemp((byte) 1, (char) 1);
    }

    protected IDLTemp(byte b, char c) {
    }

    public IDLTemp() {
    }
}