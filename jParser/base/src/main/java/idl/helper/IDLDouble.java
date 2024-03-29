package idl.helper;

public class IDLDouble extends IDLDoubleArray {

    public static IDLDouble TMP_1 = new IDLDouble();
    public static IDLDouble TMP_2 = new IDLDouble();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
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