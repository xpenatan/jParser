package idl.helper;

public class IDLArrayByte extends IDLArray {

    public static final IDLArrayByte NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayByte native_new() {
        return new IDLArrayByte((byte) 1, (char) 1);
    }

    protected IDLArrayByte(byte b, char c) {
    }

    protected IDLArrayByte() {}

    public IDLArrayByte(int size) {
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
                                 IDLArrayByte dest, int destPos,
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

    public static void arraycopy(IDLArrayByte src, int  srcPos,
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