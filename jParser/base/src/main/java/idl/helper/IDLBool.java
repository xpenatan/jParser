package idl.helper;

public class IDLBool extends IDLBoolArray {

    public static IDLBool TMP_1 = new IDLBool();
    public static IDLBool TMP_2 = new IDLBool();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLBool() {
        super(1);
    }

    public IDLBool(boolean value) {
        this();
        set(value);
    }

    public IDLBool set(boolean value) {
        setValue(0, value);
        return this;
    }

    public boolean getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}