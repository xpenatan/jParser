package idl;

/**
 * @author xpenatan
 */
public abstract class IDLBase {

    public static boolean ENABLE_LOGGING = true;
    protected final IDLNativeData nativeData = new IDLNativeData(this);

    public IDLBase() {
    }

    public final void initNative(long cPtr, boolean cMemoryOwn) {
        nativeData.initNative(cPtr, cMemoryOwn);
    }

    public final void setCPointer(long cPtr) {
        nativeData.setCPointer(cPtr);
    }

    public final long getCPointer() {
        return nativeData.getCPointer();
    }

    public final IDLNativeData getNativeData() {
        return nativeData;
    }

    public boolean isDisposed() {
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

    public void takeOwnership() {
        nativeData.takeOwnership();
    }

    public void releaseOwnership() {
        nativeData.releaseOwnership();
    }

    /*[-TEAVM;-REPLACE]
       @org.teavm.jso.JSBody(params = { "addr" }, script = "return [MODULE].UTF8ToString(addr);")
       public static native String getJSString(int addr);
    */
    public static String getJSString(long addr) {
        return null;
    }
}