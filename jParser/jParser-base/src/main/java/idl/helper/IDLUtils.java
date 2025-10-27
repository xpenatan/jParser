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
    public static void copyToByteBuffer(IDLBase source, ByteBuffer destination, long offset, long sizeInBytes) {
        internal_native_copyToByteBuffer(source.native_void_address, destination, offset, sizeInBytes);
    }

    /*[-TEAVM;-REPLACE]
        @org.teavm.jso.JSBody(params = {"data_addr", "destination_addr", "offset", "sizeInBytes" }, script = "" +
            "var dataOut = [MODULE].HEAPU8.subarray(data_addr, data_addr + sizeInBytes);" +
            "destination_addr.set(dataOut, offset);"
        )
        private static native void internal_native_copyToByteBuffer(int data_addr, org.teavm.jso.JSObject destination_addr, long offset, long sizeInBytes);
    */
    /*[-JNI;-NATIVE]
        void* data = (void*)data_addr;
        char* bufferAddress = (char*)env->GetDirectBufferAddress(destination);
        memcpy(bufferAddress + offset, data, sizeInBytes);
    */
    public static native void internal_native_copyToByteBuffer(long data_addr, ByteBuffer destination, long offset, long sizeInBytes);
}