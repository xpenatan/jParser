package idl.helper;

public class IDLIntArray extends IDLArray {

    public static final IDLIntArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLIntArray native_new() {
        return new IDLIntArray((byte) 1, (char) 1);
    }

    protected IDLIntArray() {}

    protected IDLIntArray(byte b, char c) {
    }

    public IDLIntArray(int size) {
    }

    public void copy(int[] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            int value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, int value);
    public native int getValue(int index);
}