package idl;

/**
 * @author xpenatan
 */
public class IDLBase {

    public final static IDLBase NULL;

    static {
        NULL = native_new();
    }

    public static IDLBase native_new() {
        return new IDLBase((byte)0, (char)0);
    }

    public static boolean ENABLE_LOGGING = true;

    /**
     * Native address. Used internally. Don't change.
     */

    /*[-TEAVM;-REPLACE]
       public int native_address;
    */
    public long native_address;

    /*[-TEAVM;-REPLACE]
       public int native_void_address;
    */
    public long native_void_address;

    private boolean native_cMemOwn;
    private boolean native_disposed;

    /*[-TEAVM;-REPLACE]
       public org.teavm.jso.JSObject native_object;
    */
    public Object native_object;

    public IDLBase() {
    }

    @Deprecated()
    public IDLBase(byte b, char c) {
    }

    /*[-TEAVM;-REPLACE_RAW]
       public final void internal_reset(int address, boolean cMemoryOwn) {
            dispose();
            native_cMemOwn = cMemoryOwn;
            this.native_address = address;
            native_disposed = false;
            native_object = null;
            if(address != 0) {
                onNativeAddressChanged();
            }
        }
    */
    @Deprecated
    public final void internal_reset(long address, boolean cMemoryOwn) {
        // This metho cannot be called from outside
        dispose();
        native_cMemOwn = cMemoryOwn;
        this.native_address = address;
        native_void_address = address;
        native_disposed = false;
        native_object = null;
        if(address != 0) {
            onNativeAddressChanged();
        }
    }

    /*[-TEAVM;-REPLACE]
        public final IDLBase native_setVoid(long voidValue) {
            native_address = (int)voidValue;
            native_void_address = (int)voidValue;
            return this;
        }
    */
    public final IDLBase native_setVoid(long voidValue) {
        native_address = voidValue;
        native_void_address = voidValue;
        return this;
    }

    public final IDLBase native_setVoid(int voidValue) {
        native_address = voidValue;
        native_void_address = voidValue;
        return this;
    }

    public final boolean native_isNULL() {
        return native_address == 0;
    }

    /**
     * Reset this instance to default state. Use only in instance created by you.
     * Caution: Resetting an owned native instance will cause a memory leak if not disposed.
     */
    public final IDLBase native_reset() {
        native_cMemOwn = false;
        native_disposed = false;
        native_object = null;
        native_address = 0;
        native_void_address = 0;
        return this;
    }

    /**
     * Take ownership of the native instance, causing the native object to be warned when this object gets out of scope without disposing.
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

    /**
     * Copy the address data and set ownership/disposed to false
     */
    public final void native_copy(IDLBase other) {
        this.native_address = other.native_address;
        this.native_object = other.native_object;
        this.native_void_address = other.native_void_address;
        this.native_disposed = false;
        this.native_cMemOwn = false;
    }

    public final boolean isDisposed() {
        return native_disposed;
    }

    public final void dispose() {
        if(native_cMemOwn) {
            if(!native_disposed) {
                if(native_address != 0) {
                    native_disposed = true;
                    deleteNative();
                    native_address = 0;
                    native_void_address = 0;
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
                    error("IDL", "Disposing warning - " + this + " is already disposed");
                }
            }
        }
        else {
            if(IDLBase.ENABLE_LOGGING) {
                error("IDL", "Disposing warning - " + this + " is not memory owned");
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