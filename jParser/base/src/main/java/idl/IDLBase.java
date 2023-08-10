package idl;

/**
 * @author xpenatan
 */
public abstract class IDLBase {

    public static boolean USE_REF_COUNTING = false;
    public static boolean ENABLE_LOGGING = true;

    protected long cPointer;
    protected boolean cMemOwn;
    private boolean disposed;
    protected boolean destroyed;
    private int refCount;

    public IDLBase() {
    }

    protected void initObject(long cPtr, boolean cMemoryOwn) {
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
    }

    /**
     * Set pointer if it's not owned by this object. Useful for setting temp objets
     */
    public void setPointer(long cPtr) {
        if(!cMemOwn) {
            cPointer = cPtr;
        }
    }

    /**
     * Obtains a reference to this object, call release to free the reference.
     */
    public void obtain() {
        refCount++;
    }

    /**
     * Release a previously obtained reference, causing the object to be disposed when this was the last reference.
     */
    public void release() {
        if(--refCount <= 0 && USE_REF_COUNTING) dispose();
    }

    /**
     * @return Whether this instance is obtained using the {@link #obtain()} method.
     */
    public boolean isObtained() {
        return refCount > 0;
    }

    protected void construct() {
        destroyed = false;
    }

    protected void reset(long cPtr, boolean cMemoryOwn) {
        if(!destroyed) destroy();
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
        construct();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IDLBase) && (((IDLBase)obj).cPointer == this.cPointer);
    }

    @Override
    public int hashCode() {
        return (int)cPointer;
    }

    /**
     * @return The memory location (pointer) of this instance.
     */
    public long getCPointer() {
        return cPointer;
    }

    /**
     * Take ownership of the native instance, causing the native object to be deleted when this object gets out of scope.
     */
    public void takeOwnership() {
        cMemOwn = true;
    }

    /**
     * Release ownership of the native instance, causing the native object NOT to be deleted when this object gets out of
     * scope.
     */
    public void releaseOwnership() {
        cMemOwn = false;
    }

    /**
     * @return True if the native is destroyed when this object gets out of scope, false otherwise.
     */
    public boolean hasOwnership() {
        return cMemOwn;
    }

    /**
     * Deletes the IDL object this class encapsulates. Do not call directly, instead use the {@link #dispose()} method.
     */
    protected void deleteNative() {

    }

    public void dispose() {
        if(refCount > 0 && USE_REF_COUNTING && ENABLE_LOGGING) {
            error("IDL", "Disposing " + toString() + " while it still has " + refCount + " references.");
        }
        if(cMemOwn) {
            // Don't try to delete if this object did not create the pointer
            disposed = true;
            deleteNative();
            cPointer = 0;
        }
    }

    /**
     * @return Whether the {@link #dispose()} method of this instance is called.
     */
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + cPointer + "," + cMemOwn + ")";
    }

    protected void destroy() {
        try {
            if(destroyed && ENABLE_LOGGING) {
                error("IDL", "Already destroyed " + toString());
            }
            destroyed = true;

            if(cMemOwn && !disposed) {
                if(ENABLE_LOGGING) {
                    error("IDL", "Disposing " + toString() + " due to garbage collection.");
                }
                dispose();
            }
        } catch(Throwable e) {
            error("IDL", "Exception while destroying " + toString(), e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if(!destroyed && ENABLE_LOGGING) {
            error("IDL", "The " + getClass().getSimpleName() + " class does not override the finalize method.");
        }
        super.finalize();
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(String tag, String message) {
        //TODO impl
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(String tag, String message, Throwable exception) {
        //TODO impl
    }
}