package runtime.helper;

import com.github.xpenatan.jParser.api.NativeObject;
import java.nio.ByteBuffer;

public class NativeUtils {

    private static final Object NULL_SEGMENT = null;

    /*[-JNI;-NATIVE]
        #include <cstring>
    */

    /*[-TEAVM;-REPLACE]
       @org.teavm.jso.JSBody(params = { "addr" }, script = "return [MODULE].UTF8ToString(addr);")
       public static native String getJSString(int addr);
    */
    public static String getJSString(long addr) {
        return null;
    }

    /**
     * Converts a Java String to a native UTF-8 C string segment.
     * FFM path selects the encoder once at class init to avoid per-call strategy branching.
     */
    /*[-FFM;-ADD]
        private static final boolean FFM_STRING_CACHE = java.lang.Boolean.getBoolean("jparser.ffm.stringCache");
    */
    /*[-FFM;-ADD]
        private static final String FFM_STRING_STRATEGY = java.lang.System.getProperty("jparser.ffm.stringStrategy", "").trim().toLowerCase(java.util.Locale.ROOT);
    */
    /*[-FFM;-ADD]
        private static final int FFM_STRING_RING_RESET_EVERY = java.lang.Integer.getInteger("jparser.ffm.stringRingResetEvery", 512);
    */
    /*[-FFM;-ADD]
        private static final int FFM_STRING_CACHE_SIZE = java.lang.Integer.getInteger("jparser.ffm.stringCacheSize", 256);
    */
    /*[-FFM;-ADD]
        private static final ThreadLocal<java.util.LinkedHashMap<String, java.lang.foreign.MemorySegment>> FFM_STRING_SEGMENTS = ThreadLocal.withInitial(() -> new java.util.LinkedHashMap<String, java.lang.foreign.MemorySegment>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, java.lang.foreign.MemorySegment> eldest) {
                return size() > FFM_STRING_CACHE_SIZE;
            }
        });
    */
    /*[-FFM;-ADD]
        private static final ThreadLocal<CStringRingState> FFM_STRING_RING = ThreadLocal.withInitial(() -> new CStringRingState(FFM_STRING_RING_RESET_EVERY));
    */
    /*[-FFM;-ADD]
        private interface CStringEncoder {
            java.lang.foreign.MemorySegment encode(String value);
        }
    */
    /*[-FFM;-ADD]
        private static final CStringEncoder FFM_CSTRING_ENCODER = initCStringEncoder();
    */
    /*[-FFM;-ADD]
        private static CStringEncoder initCStringEncoder() {
            if(FFM_STRING_STRATEGY.isEmpty()) {
                if(FFM_STRING_CACHE) {
                    return NativeUtils::toCStringCache;
                }
                // Default to ring mode for max throughput without runtime flag tuning.
                return NativeUtils::toCStringRing;
            }
            if("ring".equals(FFM_STRING_STRATEGY)) {
                return NativeUtils::toCStringRing;
            }
            if("cache".equals(FFM_STRING_STRATEGY)) {
                return NativeUtils::toCStringCache;
            }
            if("global".equals(FFM_STRING_STRATEGY)) {
                return NativeUtils::toCStringGlobal;
            }
            return NativeUtils::toCStringGlobal;
        }
    */
    /*[-FFM;-ADD]
        private static java.lang.foreign.MemorySegment toCStringGlobal(String value) {
            return java.lang.foreign.Arena.global().allocateFrom(value);
        }
    */
    /*[-FFM;-ADD]
        private static java.lang.foreign.MemorySegment toCStringRing(String value) {
            return FFM_STRING_RING.get().allocate(value);
        }
    */
    /*[-FFM;-ADD]
        private static java.lang.foreign.MemorySegment toCStringCache(String value) {
            java.util.LinkedHashMap<String, java.lang.foreign.MemorySegment> cache = FFM_STRING_SEGMENTS.get();
            java.lang.foreign.MemorySegment segment = cache.get(value);
            if(segment == null) {
                segment = java.lang.foreign.Arena.global().allocateFrom(value);
                cache.put(value, segment);
            }
            return segment;
        }
    */
    /*[-FFM;-ADD]
        private static final class CStringRingState {
            private final int resetEvery;
            private java.lang.foreign.Arena arena;
            private int usageCount;

            private CStringRingState(int resetEvery) {
                this.resetEvery = java.lang.Math.max(1, resetEvery);
            }

            private java.lang.foreign.MemorySegment allocate(String value) {
                if(arena == null || usageCount >= resetEvery) {
                    if(arena != null) {
                        arena.close();
                    }
                    arena = java.lang.foreign.Arena.ofConfined();
                    usageCount = 0;
                }
                usageCount++;
                return arena.allocateFrom(value);
            }
        }
    */
    /*[-FFM;-REPLACE]
        public static java.lang.foreign.MemorySegment toCString(String value) {
            if(value == null) {
                return java.lang.foreign.MemorySegment.NULL;
            }
            return FFM_CSTRING_ENCODER.encode(value);
        }
    */
    public static Object toCString(String value) {
        return NULL_SEGMENT;
    }

    /**
     * Returns the base native address for a direct ByteBuffer.
     * FFM implementation normalizes the buffer view to start at zero and restores state.
     */
    /*[-FFM;-ADD]
        private static final int DIRECT_BUFFER_CACHE_SIZE = java.lang.Integer.getInteger("jparser.ffm.bufferCacheSize", 4);
    */
    /*[-FFM;-ADD]
        private static final ThreadLocal<java.util.LinkedHashMap<ByteBuffer, Long>> DIRECT_BUFFER_CACHE = ThreadLocal.withInitial(() -> new java.util.LinkedHashMap<ByteBuffer, Long>(DIRECT_BUFFER_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<ByteBuffer, Long> eldest) {
                return size() > DIRECT_BUFFER_CACHE_SIZE;
            }
        });
    */
    /*[-FFM;-REPLACE]
        public static long address(ByteBuffer byteBuffer) {
            if(byteBuffer == null) {
                return 0L;
            }
            if(!byteBuffer.isDirect()) {
                throw new IllegalArgumentException("Direct ByteBuffer required");
            }

            java.util.LinkedHashMap<ByteBuffer, Long> cache = DIRECT_BUFFER_CACHE.get();
            Long cachedAddress = cache.get(byteBuffer);
            if(cachedAddress != null) {
                return cachedAddress.longValue();
            }

            int oldPos = byteBuffer.position();
            int oldLimit = byteBuffer.limit();
            long address;
            try {
                byteBuffer.position(0);
                byteBuffer.limit(byteBuffer.capacity());
                address = java.lang.foreign.MemorySegment.ofBuffer(byteBuffer).address();
            }
            finally {
                byteBuffer.position(oldPos);
                byteBuffer.limit(oldLimit);
            }

            cache.put(byteBuffer, address);
            return address;
        }
    */
    public static long address(ByteBuffer byteBuffer) {
        return 0L;
    }

    private static final boolean BATCH_ARRAY_COPY = Boolean.getBoolean("jparser.ffm.batchArrayCopy");
    private static final int BATCH_ARRAY_COPY_MIN_BYTES = Integer.getInteger("jparser.ffm.batchArrayCopyMinBytes", 128);
    private static final int BATCH_ARRAY_COPY_SCRATCH_MIN = Integer.getInteger("jparser.ffm.batchArrayCopyScratchMin", 4096);
    private static final ThreadLocal<ByteBuffer> BATCH_COPY_SCRATCH = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(BATCH_ARRAY_COPY_SCRATCH_MIN));

    public static boolean useBatchArrayCopy(int sizeInBytes) {
        return BATCH_ARRAY_COPY && sizeInBytes >= BATCH_ARRAY_COPY_MIN_BYTES;
    }

    public static void copyByteArrayToNative(byte[] source, int srcPos, NativeObject destination, int destOffset, int sizeInBytes) {
        if(sizeInBytes <= 0) {
            return;
        }
        ByteBuffer scratch = getBatchScratch(sizeInBytes);
        scratch.put(source, srcPos, sizeInBytes);
        copyFromByteBuffer(scratch, destination, destOffset, sizeInBytes);
    }

    public static void copyByteArrayFromNative(NativeObject source, int srcOffset, byte[] destination, int destPos, int sizeInBytes) {
        if(sizeInBytes <= 0) {
            return;
        }
        ByteBuffer scratch = getBatchScratch(sizeInBytes);
        copyToByteBuffer(source, scratch, srcOffset, sizeInBytes);
        scratch.get(destination, destPos, sizeInBytes);
    }

    private static ByteBuffer getBatchScratch(int sizeInBytes) {
        ByteBuffer scratch = BATCH_COPY_SCRATCH.get();
        if(scratch.capacity() < sizeInBytes) {
            int capacity = Math.max(sizeInBytes, BATCH_ARRAY_COPY_SCRATCH_MIN);
            scratch = ByteBuffer.allocateDirect(capacity);
            BATCH_COPY_SCRATCH.set(scratch);
        }
        scratch.clear();
        scratch.limit(sizeInBytes);
        return scratch;
    }

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            org.teavm.jso.typedarrays.Int8Array destinationArray = org.teavm.jso.typedarrays.Int8Array.fromJavaBuffer(destination);
            internal_native_copyToByteBuffer((int)source.native_void_address, destinationArray, offset, sizeInBytes);
        }
    */
    /*[-FFM;-REPLACE_BLOCK]
        {
            long destinationAddress = NativeUtils.address(destination);
            internal_native_copyToByteBuffer(source.native_void_address, destinationAddress, offset, sizeInBytes);
        }
    */
    public static void copyToByteBuffer(NativeObject source, ByteBuffer destination, int offset, int sizeInBytes) {
        internal_native_copyToByteBuffer(source.native_void_address, destination, offset, sizeInBytes);
    }

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            org.teavm.jso.typedarrays.Int8Array sourceArray = org.teavm.jso.typedarrays.Int8Array.fromJavaBuffer(source);
            internal_native_copyFromByteBuffer(sourceArray, (int)destination.native_void_address, offset, sizeInBytes);
        }
    */
    /*[-FFM;-REPLACE_BLOCK]
        {
            internal_native_copyFromByteBuffer(source, destination.native_void_address, offset, sizeInBytes);
        }
    */
    public static void copyFromByteBuffer(ByteBuffer source, NativeObject destination, int offset, int sizeInBytes) {
        internal_native_copyFromByteBuffer(source, destination.native_void_address, offset, sizeInBytes);
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"data_addr", "destination_addr", "offset", "sizeInBytes" }, script = "" +
            "var dataOut = [MODULE].HEAPU8.subarray(data_addr, data_addr + sizeInBytes);" +
            "destination_addr.set(dataOut, offset);"
        )
        private static native void internal_native_copyToByteBuffer(int data_addr, org.teavm.jso.JSObject destination_addr, int offset, int sizeInBytes);
    */
    /*[-JNI;-NATIVE]
        void* data = (void*)data_addr;
        char* bufferAddress = (char*)env->GetDirectBufferAddress(destination);
        memcpy(bufferAddress + offset, data, sizeInBytes);
    */
    /*[-FFM;-NATIVE]
        #include <cstring>
        extern "C" {
        FFM_EXPORT void jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyToByteBuffer__JJII(int64_t data_addr, int64_t destination_addr, int32_t offset, int32_t sizeInBytes) {
            void* data = (void*)data_addr;
            char* bufferAddress = (char*)destination_addr;
            memcpy(bufferAddress + offset, data, sizeInBytes);
        }
        }
    */
    /*[-FFM;-REPLACE]
        public static void internal_native_copyToByteBuffer(long data_addr, long destination_addr, int offset, int sizeInBytes) {
            try {
                FFMHandles.internal_native_copyToByteBuffer.invokeExact(data_addr, destination_addr, offset, sizeInBytes);
            } catch(Throwable e) { throw new RuntimeException(e); }
        }
    */
    public static native void internal_native_copyToByteBuffer(long data_addr, ByteBuffer destination, int offset, int sizeInBytes);

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"source_addr", "data_addr", "offset", "sizeInBytes"}, script = "" +
            "var dataIn = source_addr.subarray(0, sizeInBytes);" +
            "[MODULE].HEAPU8.set(dataIn, data_addr + offset);"
        )
        private static native void internal_native_copyFromByteBuffer(org.teavm.jso.JSObject source_addr, int data_addr, int offset, int sizeInBytes);
    */
    /*[-JNI;-NATIVE]
        void* data = (void*)data_addr;
        char* sourceAddress = (char*)env->GetDirectBufferAddress(source);
        memcpy((char*)data + offset, sourceAddress, sizeInBytes);
    */
    /*[-FFM;-NATIVE]
        #include <cstring>
        extern "C" {
        FFM_EXPORT void jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyFromByteBuffer__JJII(int64_t source_addr, int64_t data_addr, int32_t offset, int32_t sizeInBytes) {
            void* data = (void*)data_addr;
            char* sourceAddress = (char*)source_addr;
            memcpy((char*)data + offset, sourceAddress, sizeInBytes);
        }
        }
    */
    /*[-FFM;-REPLACE]
        public static void internal_native_copyFromByteBuffer(ByteBuffer source, long data_addr, int offset, int sizeInBytes) {
            long source_addr = NativeUtils.address(source);
            try {
                FFMHandles.internal_native_copyFromByteBuffer.invokeExact(source_addr, data_addr, offset, sizeInBytes);
            } catch(Throwable e) { throw new RuntimeException(e); }
        }
    */
    public static native void internal_native_copyFromByteBuffer(ByteBuffer source, long data_addr, int offset, int sizeInBytes);

    /*[-FFM;-ADD]
        private static final class FFMHandles {
            private static final java.lang.foreign.Linker.Option[] LINKER_OPTIONS_CRITICAL = new java.lang.foreign.Linker.Option[] { java.lang.foreign.Linker.Option.critical(true) };
            private static final java.lang.foreign.Linker.Option[] LINKER_OPTIONS_DEFAULT = new java.lang.foreign.Linker.Option[0];
            private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
            private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();

            static final java.lang.invoke.MethodHandle internal_native_copyToByteBuffer;
            static {
                java.lang.foreign.MemorySegment symbol = LOOKUP.find("jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyToByteBuffer__JJII").orElseThrow();
                java.lang.foreign.FunctionDescriptor descriptor = java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT);
                java.lang.invoke.MethodHandle handle;
                try {
                    handle = LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_CRITICAL);
                }
                catch(Throwable ignored) {
                    handle = LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_DEFAULT);
                }
                internal_native_copyToByteBuffer = handle;
            }

            static final java.lang.invoke.MethodHandle internal_native_copyFromByteBuffer;
            static {
                java.lang.foreign.MemorySegment symbol = LOOKUP.find("jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyFromByteBuffer__JJII").orElseThrow();
                java.lang.foreign.FunctionDescriptor descriptor = java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT);
                java.lang.invoke.MethodHandle handle;
                try {
                    handle = LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_CRITICAL);
                }
                catch(Throwable ignored) {
                    handle = LINKER.downcallHandle(symbol, descriptor, LINKER_OPTIONS_DEFAULT);
                }
                internal_native_copyFromByteBuffer = handle;
            }
        }
    */
}
