package idl.helper;

public class IDLBoolArray extends IDLArrayBase {

    public static final IDLBoolArray NULL = createInstance();

    /**
     * @return An empty instance without a native address
     */
    public static IDLBoolArray createInstance() {
        return new IDLBoolArray((byte) 1, (char) 1);
    }

    protected IDLBoolArray(byte b, char c) {
    }

    public IDLBoolArray(int size) {
    }

    public void copy(boolean [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            boolean value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, boolean value);
    public native boolean getValue(int index);
}