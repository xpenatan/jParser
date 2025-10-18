package idl.helper;

public class IDLArrayFloat extends IDLArray {


    public static final IDLArrayFloat NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayFloat native_new() {
        return new IDLArrayFloat((byte) 1, (char) 1);
    }

    protected IDLArrayFloat() {}

    protected IDLArrayFloat(byte b, char c) {
    }

    public IDLArrayFloat(int size) {
    }

    public void copy(float [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            float value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, float value);
    public native float getValue(int index);
}