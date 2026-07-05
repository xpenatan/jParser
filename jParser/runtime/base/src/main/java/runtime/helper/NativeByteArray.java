package runtime.helper;

public class NativeByteArray extends NativeArray {

    public static final NativeByteArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static NativeByteArray native_new() {
        return new NativeByteArray((byte) 1, (char) 1);
    }

    protected NativeByteArray(byte b, char c) {
    }

    protected NativeByteArray() {}

    public NativeByteArray(int size) {
    }

    public void copy(byte [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            byte value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, byte value);
    public native byte getValue(int index);

    public static void arraycopy(byte[] src, int  srcPos,
                                 NativeByteArray dest, int destPos,
                                 int length) {
        if(length <= 0) {
            return;
        }
        if(NativeUtils.useBatchArrayCopy(length)) {
            NativeUtils.copyByteArrayToNative(src, srcPos, dest, destPos, length);
            return;
        }
        int srcP = srcPos;
        int destP = destPos;
        int count = 0;
        while(count < length) {
            byte srcByte = src[srcP];
            srcP++;
            dest.setValue(destP, srcByte);
            destP++;
            count++;
        }
    }

    public static void arraycopy(NativeByteArray src, int  srcPos,
                                 byte[] dest, int destPos,
                                 int length) {
        if(length <= 0) {
            return;
        }
        if(NativeUtils.useBatchArrayCopy(length)) {
            NativeUtils.copyByteArrayFromNative(src, srcPos, dest, destPos, length);
            return;
        }
        int srcP = srcPos;
        int destP = destPos;
        int count = 0;
        while(count < length) {
            byte srcByte = src.getValue(srcP);
            srcP++;
            dest[destP] = srcByte;
            destP++;
            count++;
        }
    }
}
