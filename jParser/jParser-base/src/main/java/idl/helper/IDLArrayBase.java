package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;

public class IDLArrayBase extends IDLBase {

    @Override
    protected void onNativeAddressChanged() {
        native_void_address = getPointer();
    }

    public void resize(int size) {
        internal_native_resize(native_address, size);
        native_void_address = getPointer();
    }

    public native long getPointer();
    public native int getSize();

    /*[-TEAVM;-NATIVE]
      var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].IDLArrayBase);
      jsObj.resize(size);
    */
    /*[-JNI;-NATIVE]
      IDLArrayBase* nativeObject = (IDLArrayBase*)this_addr;
      nativeObject->resize(size);
    */
    public static native void internal_native_resize(long this_addr, int size);
}
