package idl;

/**
 * @author xpenatan
 */
public abstract class IDLBase {

    public static boolean ENABLE_LOGGING = true;
    protected IDLNativeData nativeData = new IDLNativeData(this);

    public IDLBase() {
    }

    public final IDLNativeData getNativeData() {
        return nativeData;
    }

    protected boolean isDisposed() {
        return nativeData.isDisposed();
    }

    protected void dispose() {
        nativeData.dispose();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + nativeData;
    }

    /**
     * Deletes the IDL object this class encapsulates. Do not call directly, instead use the {@link #dispose()} method.
     */
    protected void deleteNative() {
    }

    protected void onNativeAddressChanged() {

    }

    protected void onNativeDispose() {
        nativeData = null;
    }

    /*[-TEAVM;-REPLACE]
       @org.teavm.jso.JSBody(params = { "addr" }, script = "return [MODULE].UTF8ToString(addr);")
       public static native String getJSString(int addr);
    */
    public static String getJSString(long addr) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return nativeData.equals(obj);
    }
}