package idl.helper;

public class IDLFloat extends IDLFloatArray {

    public static IDLFloat TMP_1 = new IDLFloat();
    public static IDLFloat TMP_2 = new IDLFloat();
    public static IDLFloat TMP_3 = new IDLFloat();
    public static IDLFloat TMP_4 = new IDLFloat();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
        TMP_3.dispose();
        TMP_4.dispose();
    }

    public IDLFloat() {
        super(1);
    }

    public IDLFloat(float value) {
        this();
        set(value);
    }

    public IDLFloat set(float value) {
        setValue(0, value);
        return this;
    }

    public float getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}