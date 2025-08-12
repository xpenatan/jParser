package idl.helper;

import idl.IDLBase;

public class IDLArrayBase extends IDLBase {

    /*[-TEAVM;-REPLACE]
       public int native_array_address;
    */
    public long native_array_address;

    @Override
    protected void onNativeAddressChanged() {
        native_array_address = getPointer();
    }

    public void resize(int size) {
        internal_native_resize(native_address, size);
        native_array_address = getPointer();
    }

    public native long getPointer();
    public native int getSize();

    public static native void internal_native_resize(long this_addr, int size);
}
