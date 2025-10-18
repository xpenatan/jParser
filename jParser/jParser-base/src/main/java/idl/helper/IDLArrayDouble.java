package idl.helper;

public class IDLArrayDouble extends IDLArray {

    public static final IDLArrayDouble NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLArrayDouble native_new() {
        return new IDLArrayDouble((byte) 1, (char) 1);
    }

    protected IDLArrayDouble(byte b, char c) {
    }

    protected IDLArrayDouble() {}

    public IDLArrayDouble(int size) {
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