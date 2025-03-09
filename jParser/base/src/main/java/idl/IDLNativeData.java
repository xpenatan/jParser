package idl;

public class IDLNativeData {

    private IDLBase idlBase;
    private long cPointer;
    private boolean cMemOwn;
    private boolean disposed;

    public IDLNativeData(IDLBase idlBase) {
        this.idlBase = idlBase;
    }

    public void initNative(long cPtr, boolean cMemoryOwn) {
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
    }

    public void reset(long cPtr, boolean cMemoryOwn) {
        dispose();
        cMemOwn = cMemoryOwn;
        cPointer = cPtr;
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
     * Take ownership of the native instance, causing the native object to be warned when this object gets out of scope.
     */
    public void takeOwnership() {
        cMemOwn = true;
    }

    /**
     * Release ownership of the native instance, causing the native object NOT to be warned when this object gets out of
     * scope.
     */
    public void releaseOwnership() {
        cMemOwn = false;
    }

    public boolean hasOwnership() {
        return cMemOwn;
    }

    public void dispose() {
        if(cMemOwn) {
            if(!disposed) {
                if(cPointer != 0) {
                    disposed = true;
                    idlBase.deleteNative();
                    cPointer = 0;
                }
                else {
                    if(IDLBase.ENABLE_LOGGING) {
                        error("IDL", "Disposing error - " + toString() + " cPointer is 0");
                    }
                }
            }
            else {
                if(IDLBase.ENABLE_LOGGING) {
                    error("IDL", "Disposing error - " + toString() + " is already disposed");
                }
            }
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

    @Override
    protected void finalize() throws Throwable {
        if(cMemOwn && !disposed && IDLBase.ENABLE_LOGGING) {
            error("IDL", " Memory Leak - " + idlBase.getClass().getSimpleName() + " was not disposed correctly.");
        }
        super.finalize();
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(String tag, String message) {
        System.err.println(tag + ": " + message);
    }
}
