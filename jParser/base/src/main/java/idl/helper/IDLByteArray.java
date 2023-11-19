package idl.helper;

import idl.IDLBase;

public class IDLByteArray extends IDLBase {

    public static IDLByteArray tmp1_1 = new IDLByteArray(1);
    public static IDLByteArray tmp1_2 = new IDLByteArray(1);

    public static IDLByteArray tmp2_1 = new IDLByteArray(2);
    public static IDLByteArray tmp2_2 = new IDLByteArray(2);

    public static IDLByteArray tmp3_1 = new IDLByteArray(3);
    public static IDLByteArray tmp3_2 = new IDLByteArray(3);

    public static IDLByteArray tmp4_1 = new IDLByteArray(4);
    public static IDLByteArray tmp4_2 = new IDLByteArray(4);

    public static void disposeTEMP() {
        tmp1_1.dispose();
        tmp1_2.dispose();
        tmp2_1.dispose();
        tmp2_2.dispose();
        tmp3_1.dispose();
        tmp3_2.dispose();
        tmp4_1.dispose();
        tmp4_2.dispose();
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
    public native void resize(int size);
    public native byte getValue(int index);
    public native long getPointer();
    public native int getSize();

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