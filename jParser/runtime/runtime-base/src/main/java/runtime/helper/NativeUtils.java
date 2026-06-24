package runtime.helper;

import com.github.xpenatan.jParser.api.NativeObject;
import java.nio.ByteBuffer;

public class NativeUtils {
    /*[-TEAVM_C;-NATIVE]
        #include <cstring>

        extern "C" {
        TEAVMC_EXPORT void jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_copyToByteBuffer_l_l_i_i(int64_t data_addr, int64_t destination_addr, int32_t offset, int32_t sizeInBytes) {
            void* data = (void*)data_addr;
            char* bufferAddress = (char*)destination_addr;
            memcpy(bufferAddress + offset, data, sizeInBytes);
        }

        TEAVMC_EXPORT void jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_copyFromByteBuffer_l_l_i_i(int64_t source_addr, int64_t data_addr, int32_t offset, int32_t sizeInBytes) {
            void* data = (void*)data_addr;
            char* sourceAddress = (char*)source_addr;
            memcpy((char*)data + offset, sourceAddress, sizeInBytes);
        }

        TEAVMC_EXPORT int8_t jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_readByte_l_i(int64_t data_addr, int32_t offset) {
            return *((int8_t*)data_addr + offset);
        }

        TEAVMC_EXPORT void jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_writeByte_l_i_b(int64_t data_addr, int32_t offset, int8_t value) {
            *((int8_t*)data_addr + offset) = value;
        }
        }
    */

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
    /*[-FFM;-REPLACE]
        public static java.lang.foreign.MemorySegment toCString(String value) {
            if(value == null) {
                return java.lang.foreign.MemorySegment.NULL;
            }
            return toCStringCache(value);
        }
    */
    public static Object toCString(String value) {
        return NULL_SEGMENT;
    }

    /**
     * Returns the base native address for a direct ByteBuffer.
     * FFM implementation normalizes the buffer view to start at zero and restores state.
     */
    /*[-FFM;-REPLACE]
        public static long address(ByteBuffer byteBuffer) {
            if(byteBuffer == null) {
                return 0L;
            }
            if(!byteBuffer.isDirect()) {
                throw new IllegalArgumentException("Direct ByteBuffer required");
            }

            // Fast path for callers that already pass a normalized full-capacity view.
            if(byteBuffer.position() == 0 && byteBuffer.limit() == byteBuffer.capacity()) {
                return java.lang.foreign.MemorySegment.ofBuffer(byteBuffer).address();
            }

            int oldPos = byteBuffer.position();
            int oldLimit = byteBuffer.limit();
            try {
                byteBuffer.position(0);
                byteBuffer.limit(byteBuffer.capacity());
                return java.lang.foreign.MemorySegment.ofBuffer(byteBuffer).address();
            }
            finally {
                byteBuffer.position(oldPos);
                byteBuffer.limit(oldLimit);
            }
        }
    */
    /*[-TEAVM_C;-REPLACE]
        public static long address(ByteBuffer byteBuffer) {
            if(byteBuffer == null) {
                return 0L;
            }
            org.teavm.classlib.java.nio.file.TAddressBasedBuffer addressBasedBuffer = (org.teavm.classlib.java.nio.file.TAddressBasedBuffer)(Object)byteBuffer;
            return addressBasedBuffer.getDataAddress().toLong();
        }
    */
    public static long address(ByteBuffer byteBuffer) {
        return 0L;
    }

    private static final boolean BATCH_ARRAY_COPY = Boolean.parseBoolean(System.getProperty("jparser.ffm.batchArrayCopy", "true"));
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
    /*[-TEAVM_C;-REPLACE_BLOCK]
        {
            long sourceAddress = source.native_void_address;
            for(int i = 0; i < sizeInBytes; i++) {
                destination.put(offset + i, internal_native_readByte(sourceAddress, i));
            }
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
    /*[-TEAVM_C;-REPLACE_BLOCK]
        {
            long destinationAddress = destination.native_void_address;
            for(int i = 0; i < sizeInBytes; i++) {
                internal_native_writeByte(destinationAddress, offset + i, source.get(i));
            }
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
    /*[-TEAVM_C;-REPLACE]
        @org.teavm.interop.Import(name = "jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_copyToByteBuffer_l_l_i_i")
        public static native void internal_native_copyToByteBuffer(long data_addr, long destination_addr, int offset, int sizeInBytes);
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
    /*[-TEAVM_C;-REPLACE]
        @org.teavm.interop.Import(name = "jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_copyFromByteBuffer_l_l_i_i")
        public static native void internal_native_copyFromByteBuffer(long source_addr, long data_addr, int offset, int sizeInBytes);
    */
    public static native void internal_native_copyFromByteBuffer(ByteBuffer source, long data_addr, int offset, int sizeInBytes);

    /*[-TEAVM_C;-REPLACE]
        @org.teavm.interop.Import(name = "jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_readByte_l_i")
        private static native byte internal_native_readByte(long data_addr, int offset);
    */
    private static native byte internal_native_readByte(long data_addr, int offset);

    /*[-TEAVM_C;-REPLACE]
        @org.teavm.interop.Import(name = "jparser_com_github_xpenatan_jparser_runtime_helper_NativeUtils_writeByte_l_i_b")
        private static native void internal_native_writeByte(long data_addr, int offset, byte value);
    */
    private static native void internal_native_writeByte(long data_addr, int offset, byte value);

    /*[-FFM;-ADD]
        private static final class FFMHandles {
            static final java.lang.invoke.MethodHandle internal_native_copyToByteBuffer = com.github.xpenatan.jparser.runtime.helper.FFMDowncallHelper.downcallCritical(
                    "jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyToByteBuffer__JJII",
                    java.lang.foreign.FunctionDescriptor.ofVoid(
                            java.lang.foreign.ValueLayout.JAVA_LONG,
                            java.lang.foreign.ValueLayout.JAVA_LONG,
                            java.lang.foreign.ValueLayout.JAVA_INT,
                            java.lang.foreign.ValueLayout.JAVA_INT));

            static final java.lang.invoke.MethodHandle internal_native_copyFromByteBuffer = com.github.xpenatan.jparser.runtime.helper.FFMDowncallHelper.downcallCritical(
                    "jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyFromByteBuffer__JJII",
                    java.lang.foreign.FunctionDescriptor.ofVoid(
                            java.lang.foreign.ValueLayout.JAVA_LONG,
                            java.lang.foreign.ValueLayout.JAVA_LONG,
                            java.lang.foreign.ValueLayout.JAVA_INT,
                            java.lang.foreign.ValueLayout.JAVA_INT));
        }
    */
}
