package idl;

/**
 * @author xpenatan
 */
public abstract class IDLBase {

    public static boolean USE_REF_COUNTING = false;
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

    public void dispose() {
        nativeData.dispose();
    }

    /**
     * Deletes the IDL object this class encapsulates. Do not call directly, instead use the {@link #dispose()} method.
     */
    protected void deleteNative() {
    }
}