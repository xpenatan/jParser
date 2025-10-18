package idl.helper;

public class IDLArrayBool extends IDLArray {

    public static final IDLArrayBool NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayBool native_new() {
        return new IDLArrayBool((byte) 1, (char) 1);
    }

    protected IDLArrayBool(byte b, char c) {
    }

    protected IDLArrayBool() {}

    public IDLArrayBool(int size) {
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