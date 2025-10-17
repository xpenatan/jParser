package idl.helper;

public class IDLDoubleArray extends IDLArray {

    public static final IDLDoubleArray NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDoubleArray native_new() {
        return new IDLDoubleArray((byte) 1, (char) 1);
    }

    protected IDLDoubleArray(byte b, char c) {
    }

    public IDLDoubleArray(int size) {
    }

    public void copy(double [] array) {
        int length = array.length;
        resize(length);
        for(int i = 0; i < length; i++) {
            double value = array[i];
            setValue(i, value);
        }
    }

    public native void setValue(int index, double value);
    public native double getValue(int index);
}