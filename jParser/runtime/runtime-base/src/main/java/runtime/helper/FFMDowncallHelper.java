package runtime.helper;

public final class FFMDowncallHelper {

    /*[-FFM;-ADD]
    private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
    */

    /*[-FFM;-ADD]
    private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();
    */

    /*[-FFM;-ADD]
    private static final java.lang.foreign.Linker.Option[] LINKER_OPTIONS_CRITICAL = new java.lang.foreign.Linker.Option[] { java.lang.foreign.Linker.Option.critical(true) };
    */

    /*[-FFM;-ADD]
    private static final java.lang.foreign.Linker.Option[] LINKER_OPTIONS_DEFAULT = new java.lang.foreign.Linker.Option[0];
    */

    private FFMDowncallHelper() {
    }

    public static RuntimeException rethrow(Throwable e) {
        if(e instanceof RuntimeException) {
            return (RuntimeException)e;
        }
        if(e instanceof Error) {
            throw (Error)e;
        }
        return new RuntimeException(e);
    }

    /*[-FFM;-REPLACE]
    public static java.lang.invoke.MethodHandle downcallDefault(String symbolName, java.lang.foreign.FunctionDescriptor descriptor) {
        java.lang.foreign.MemorySegment symbol = LOOKUP.find(symbolName).orElseThrow();
        return LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_DEFAULT);
    }
    */
    public static Object downcallDefault(String symbolName, Object descriptor) {
        return null;
    }

    /*[-FFM;-REPLACE]
    public static java.lang.invoke.MethodHandle downcallCritical(String symbolName, java.lang.foreign.FunctionDescriptor descriptor) {
        java.lang.foreign.MemorySegment symbol = LOOKUP.find(symbolName).orElseThrow();
        return LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_CRITICAL);
    }
    */
    public static Object downcallCritical(String symbolName, Object descriptor) {
        return null;
    }
}

