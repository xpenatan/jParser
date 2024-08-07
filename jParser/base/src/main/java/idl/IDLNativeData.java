package idl;

public class IDLNativeData {

    private IDLBase idlBase;
    private long cPointer;
    private boolean cMemOwn;
    private boolean disposed;
    private boolean destroyed;
    private int refCount;

    public IDLNativeData(IDLBase idlBase) {
        this.idlBase = idlBase;
    }

    public void initNative(long cPtr, boolean cMemoryOwn) {
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
    }

    /**
     * Set pointer if it's not owned by this object. Useful for setting temp objets
     */
    public void setCPointer(long cPtr) {
        if(!cMemOwn) {
            cPointer = cPtr;
        }
        else {
            String className = getClass().getSimpleName();
            throw new RuntimeException("Cannot change " + className + " pointer owned by native code");
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
        if(--refCount <= 0 && IDLBase.USE_REF_COUNTING) dispose();
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

    public void reset(long cPtr, boolean cMemoryOwn) {
        if(!destroyed) destroy();
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
        construct();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IDLBase) && (((IDLNativeData)obj).cPointer == this.cPointer);
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

    public void dispose() {
        if(refCount > 0 && IDLBase.USE_REF_COUNTING && IDLBase.ENABLE_LOGGING) {
            error("IDL", "Disposing " + toString() + " while it still has " + refCount + " references.");
        }
        if(cMemOwn) {
            // Don't try to delete if this object did not create the pointer
            disposed = true;
            idlBase.deleteNative();
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

    public void destroy() {
        try {
            if(destroyed && IDLBase.ENABLE_LOGGING) {
                error("IDL", "Already destroyed " + toString());
            }
            destroyed = true;

            if(cMemOwn && !disposed) {
                if(IDLBase.ENABLE_LOGGING) {
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
        if(!destroyed && IDLBase.ENABLE_LOGGING) {
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
