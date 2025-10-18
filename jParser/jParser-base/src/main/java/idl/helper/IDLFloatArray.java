package idl.helper;

public class IDLFloatArray extends IDLArray {


    public static final IDLFloatArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloatArray native_new() {
        return new IDLFloatArray((byte) 1, (char) 1);
    }

    protected IDLFloatArray() {}

    protected IDLFloatArray(byte b, char c) {
    }

    public IDLFloatArray(int size) {
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