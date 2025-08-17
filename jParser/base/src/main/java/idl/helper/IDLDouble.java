package idl.helper;

public class IDLDouble extends IDLDoubleArray {

    public static final IDLDouble NULL = createInstance();
    public static IDLDouble TMP_1 = new IDLDouble();
    public static IDLDouble TMP_2 = new IDLDouble();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble createInstance() {
        return new IDLDouble((byte) 1, (char) 1);
    }

    private IDLDouble(byte b, char c) {
        super(b, c);
    }

    public IDLDouble() {
        super(1);
    }

    public IDLDouble(double value) {
        this();
        set(value);
    }

    public IDLDouble set(double value) {
        setValue(0, value);
        return this;
    }

    public double getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}