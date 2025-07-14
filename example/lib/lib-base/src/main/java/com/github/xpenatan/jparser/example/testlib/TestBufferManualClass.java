package com.github.xpenatan.jparser.example.testlib;

import idl.IDLBase;
import java.nio.ByteBuffer;

public class TestBufferManualClass extends IDLBase {

    /*[-TEAVM;-REPLACE_BLOCK]
        {
            org.teavm.jso.typedarrays.Uint8Array array = org.teavm.jso.typedarrays.Uint8Array.fromJavaBuffer(data);
            internal_native_updateByteBuffer((int)getNativeData().getCPointer(), array, size, value);
        }
    */
    public void updateByteBuffer(ByteBuffer data, int size, byte value) {
        internal_native_updateByteBuffer(getNativeData().getCPointer(), data, size, value);
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"this_addr", "data", "size", "value"}, script = "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].TestBufferManualClass);var ptr = [MODULE]._malloc(data.length); [MODULE].HEAPU8.set(data, ptr); jsObj.updateByteBuffer(ptr, size, value); data.set([MODULE].HEAPU8.subarray(ptr, ptr + data.length)); [MODULE]._free(ptr);")
        private static native void internal_native_updateByteBuffer(int this_addr, org.teavm.jso.JSObject data, int size, byte value);
     */
    /*[-JNI;-NATIVE]
        TestBufferManualClass* nativeObject = (TestBufferManualClass*)this_addr;
        void* dataAddress = env->GetDirectBufferAddress(data);
        uint8_t* byteData = static_cast<uint8_t*>(dataAddress);
        nativeObject->updateByteBuffer(byteData, (int)size, (uint8_t)value);
    */
    private static native void internal_native_updateByteBuffer(long this_addr, ByteBuffer data, int size, byte value);
}