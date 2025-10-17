package idl.helper;

public class IDLByteArray extends IDLArray {

    public static final IDLByteArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLByteArray native_new() {
        return new IDLByteArray((byte) 1, (char) 1);
    }

    protected IDLByteArray(byte b, char c) {
    }

    public IDLByteArray(int size) {
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
                                 IDLByteArray dest, int destPos,
                                 int length) {
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

    public static void arraycopy(IDLByteArray src, int  srcPos,
                                 byte[] dest, int destPos,
                                 int length) {
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