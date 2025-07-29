package idl;

/**
 * @author xpenatan
 */
public abstract class IDLBase {

    public static boolean ENABLE_LOGGING = true;

    /**
     * Native address. Used internally. Don't change.
     */
    public long native_address;
    private boolean native_cMemOwn;
    private boolean native_disposed;

    /*[-TEAVM;-REPLACE]
       public org.teavm.jso.JSObject native_object;
    */
    public Object native_object;

    public IDLBase() {
    }

    public final void native_reset(long address, boolean cMemoryOwn) {
        dispose();
        native_cMemOwn = cMemoryOwn;
        this.native_address = address;
        native_disposed = false;
        native_object = null;
        if(address != 0) {
            onNativeAddressChanged();
        }
    }

    /**
     * Take ownership of the native instance, causing the native object to be warned when this object gets out of scope.
     */
    public final void native_takeOwnership() {
        native_cMemOwn = true;
    }

    /**
     * Release ownership of the native instance, causing the native object NOT to be warned when this object gets out of scope.
     */
    public final void native_releaseOwnership() {
        native_cMemOwn = false;
    }

    public boolean native_hasOwnership() {
        return native_cMemOwn;
    }

    protected boolean isDisposed() {
        return native_disposed;
    }

    protected void dispose() {
        if(native_cMemOwn) {
            if(!native_disposed) {
                if(native_address != 0) {
                    native_disposed = true;
                    deleteNative();
                    native_address = 0;
                    native_object = null;
                    onNativeDispose();
                }
                else {
                    if(IDLBase.ENABLE_LOGGING) {
                        error("IDL", "Disposing error - " + this + " native_address is 0");
                    }
                }
            }
            else {
                if(IDLBase.ENABLE_LOGGING) {
                    error("IDL", "Disposing error - " + this + " is already disposed");
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + native_address + "," + native_cMemOwn + ")";
    }

    /**
     * Deletes the IDL object this class encapsulates. Do not call directly, instead use the {@link #dispose()} method.
     */
    protected void deleteNative() {
    }

    protected void onNativeAddressChanged() {
    }

    protected void onNativeDispose() {
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
        if(obj instanceof IDLBase) {
            IDLBase idlBase = (IDLBase)obj;
            return idlBase.native_address == this.native_address;
        }
        return false;
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(String tag, String message) {
        System.err.println(tag + ": " + message);
    }

    // TODO change to other solution
//    @Override
//    protected void finalize() throws Throwable {
//        if(cMemOwn && !disposed && IDLBase.ENABLE_LOGGING) {
//            error("IDL", "Memory Leak - " + idlBase.getClass().getSimpleName() + " was not disposed correctly.");
//        }
//        super.finalize();
//    }
}