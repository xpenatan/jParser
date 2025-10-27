package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;

public class IDLArray extends IDLBase {

    public static final IDLArray NULL = native_new();

    public static IDLArray native_new() {
        return new IDLArray((byte) 1, (char) 1);
    }

    protected IDLArray() {}

    protected IDLArray(byte b, char c) {
    }

    @Override
    protected void onNativeAddressChanged() {
        IDLBase voidData = getVoidData();
        native_void_address = voidData.native_address;
    }

    public void resize(int size) {
        internal_native_resize(native_address, size);
        IDLBase voidData = getVoidData();
        native_void_address = voidData.native_address;
    }

    public native IDLBase getVoidData();

    public native int getSize();

    /*[-TEAVM;-NATIVE]
      var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLArray);
      jsObj.resize(size);
    */
    /*[-JNI;-NATIVE]
      IDL::IDLArray* nativeObject = (IDL::IDLArray*)this_addr;
      nativeObject->resize(size);
    */
    public static native void internal_native_resize(long this_addr, int size);
}
