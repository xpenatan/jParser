package com.github.xpenatan.jParser.example.testlib;

import com.github.xpenatan.jParser.idl.IDLBase;
import java.nio.ByteBuffer;

public class TestBufferManualClass extends IDLBase {

    /*[-FFM;-NATIVE]
        extern "C" {
        FFM_EXPORT void jparser_com_github_xpenatan_jParser_example_testlib_TestBufferManualClass_internal_1native_1updateByteBuffer__JJIB(int64_t this_addr, int64_t data_ptr, int32_t size, int8_t value) {
            TestBufferManualClass* nativeObject = (TestBufferManualClass*)this_addr;
            unsigned char* byteData = (unsigned char*)data_ptr;
            nativeObject->updateByteBuffer(byteData, (int)size, (unsigned char)value);
        }
        }
    */

    /*[-FFM;-ADD]
        private static final class FFMHandles {
            private static final java.lang.foreign.SymbolLookup LOOKUP = java.lang.foreign.SymbolLookup.loaderLookup();
            private static final java.lang.foreign.Linker LINKER = java.lang.foreign.Linker.nativeLinker();
            static final java.lang.invoke.MethodHandle updateByteBuffer = LINKER.downcallHandle(LOOKUP.find("jparser_com_github_xpenatan_jParser_example_testlib_TestBufferManualClass_internal_1native_1updateByteBuffer__JJIB").orElseThrow(), java.lang.foreign.FunctionDescriptor.ofVoid(java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_LONG, java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.JAVA_BYTE));
        }
    */

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            org.teavm.jso.typedarrays.Uint8Array array = org.teavm.jso.typedarrays.Uint8Array.fromJavaBuffer(data);
            internal_native_updateByteBuffer(native_address, array, size, value);
        }
    */
    /*[-FFM;-REPLACE_BLOCK]
        {
            java.lang.foreign.MemorySegment seg = java.lang.foreign.MemorySegment.ofBuffer(data);
            internal_native_updateByteBuffer(native_address, seg.address(), size, value);
        }
    */
    public void updateByteBuffer(ByteBuffer data, int size, byte value) {
        internal_native_updateByteBuffer(native_address, data, size, value);
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"this_addr", "data", "size", "value"}, script = "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].TestBufferManualClass);var ptr = idl._malloc(data.length); idl.HEAPU8.set(data, ptr); jsObj.updateByteBuffer(ptr, size, value); data.set(idl.HEAPU8.subarray(ptr, ptr + data.length)); idl._free(ptr);")
        private static native void internal_native_updateByteBuffer(int this_addr, org.teavm.jso.JSObject data, int size, byte value);
     */
    /*[-JNI;-NATIVE]
        TestBufferManualClass* nativeObject = (TestBufferManualClass*)this_addr;
        void* dataAddress = env->GetDirectBufferAddress(data);
        unsigned char* byteData = static_cast<unsigned char*>(dataAddress);
        nativeObject->updateByteBuffer(byteData, (int)size, (unsigned char)value);
    */
    /*[-FFM;-REPLACE]
        private static void internal_native_updateByteBuffer(long this_addr, long data_ptr, int size, byte value) {
            try { FFMHandles.updateByteBuffer.invokeExact(this_addr, data_ptr, size, value); }
            catch(Throwable e) { throw new RuntimeException(e); }
        }
    */
    private static native void internal_native_updateByteBuffer(long this_addr, ByteBuffer data, int size, byte value);
}