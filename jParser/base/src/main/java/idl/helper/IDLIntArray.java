package idl.helper;

public class IDLIntArray extends IDLArrayBase {

    public static final IDLIntArray NULL = createInstance();

    /**
     * @return An empty instance without a native address
     */
    public static IDLIntArray createInstance() {
        return new IDLIntArray((byte) 1, (char) 1);
    }

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