package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;

public class IDLArray extends IDLPointer {

    public static final IDLArray NULL = native_new();

    public static IDLArray native_new() {
        return new IDLArray((byte) 1, (char) 1);
    }

    protected IDLArray() {}

    protected IDLArray(byte b, char c) {
    }

    public void resize(int size) {
        internal_native_resize(native_address, size);
        IDLBase voidData = getVoidData();
        native_void_address = voidData.native_void_address;
    }

    public native int getSize();

    public static native void internal_native_resize(long this_addr, int size);
}
