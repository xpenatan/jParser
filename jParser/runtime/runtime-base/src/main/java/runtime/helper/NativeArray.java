package runtime.helper;

import com.github.xpenatan.jParser.api.NativeObject;

public class NativeArray extends NativeObject {

    public static final NativeArray NULL = native_new();

    public static NativeArray native_new() {
        return new NativeArray((byte) 1, (char) 1);
    }

    protected NativeArray() {}

    protected NativeArray(byte b, char c) {
    }

    @Override
    protected void onNativeAddressChanged() {
        NativeObject voidData = getVoidData();
        native_void_address = voidData.native_address;
    }

    public void resize(int size) {
        internal_native_resize(native_address, size);
        NativeObject voidData = getVoidData();
        native_void_address = voidData.native_address;
    }

    public native NativeObject getVoidData();

    public native int getSize();

    /*[-TEAVM;-NATIVE]
      var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].NativeArray);
      jsObj.resize(size);
    */
    /*[-JNI;-NATIVE]
      Native::NativeArray* nativeObject = (Native::NativeArray*)this_addr;
      nativeObject->resize(size);
    */
    /*[-FFM;-NATIVE]
      Native::NativeArray* nativeObject = (Native::NativeArray*)this_addr;
      nativeObject->resize(size);
    */
    public static native void internal_native_resize(long this_addr, int size);
}

