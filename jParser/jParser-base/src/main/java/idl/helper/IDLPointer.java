package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;

public class IDLPointer extends IDLBase {

    public static final IDLPointer NULL = native_new();

    public static IDLPointer native_new() {
        return new IDLPointer((byte) 1, (char) 1);
    }

    protected IDLPointer() {}

    protected IDLPointer(byte b, char c) {
    }

    @Override
    protected void onNativeAddressChanged() {
        IDLBase voidData = getVoidData();
        native_void_address = voidData.native_void_address;
    }

    public native IDLBase getVoidData();
}
