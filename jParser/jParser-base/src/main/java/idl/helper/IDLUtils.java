package idl.helper;

import com.github.xpenatan.jParser.idl.IDLBase;
import java.nio.ByteBuffer;

public class IDLUtils {

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

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            org.teavm.jso.typedarrays.Int8Array destinationArray = org.teavm.jso.typedarrays.Int8Array.fromJavaBuffer(destination);
            internal_native_copyToByteBuffer((int)source.native_void_address, destinationArray, offset, sizeInBytes);
        }
    */
    /*[-FFM;-REPLACE_BLOCK]
        {
            java.lang.foreign.MemorySegment seg = java.lang.foreign.MemorySegment.ofBuffer(destination);
            internal_native_copyToByteBuffer(source.native_void_address, seg.address(), offset, sizeInBytes);
        }
    */
    public static void copyToByteBuffer(IDLBase source, ByteBuffer destination, int offset, int sizeInBytes) {
        internal_native_copyToByteBuffer(source.native_void_address, destination, offset, sizeInBytes);
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
    /*[-FFM;-ADD]
        private static final class FFMHandles {
            private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
            private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();
            static final java.lang.invoke.MethodHandle internal_native_copyToByteBuffer = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jparser_idl_helper_IDLUtils_internal_1native_1copyToByteBuffer__JJII").orElseThrow(), java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_INT));
        }
    */
    public static native void internal_native_copyToByteBuffer(long data_addr, ByteBuffer destination, int offset, int sizeInBytes);
}