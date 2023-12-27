package idl.helper;

public class IDLInt extends IDLIntArray {

    public static IDLInt TMP_1 = new IDLInt();
    public static IDLInt TMP_2 = new IDLInt();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLInt() {
        super(1);
    }

    public IDLInt(int value) {
        this();
        set(value);
    }

    public IDLInt set(int value) {
        setValue(0, value);
        return this;
    }

    public int getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}