package idl.helper;

public class IDLInt extends IDLIntArray {

    public static final IDLInt NULL = createInstance();
    public static IDLInt TMP_1 = new IDLInt();
    public static IDLInt TMP_2 = new IDLInt();
    public static IDLInt TMP_3 = new IDLInt();
    public static IDLInt TMP_4 = new IDLInt();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
        TMP_3.dispose();
        TMP_4.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt createInstance() {
        return new IDLInt((byte) 1, (char) 1);
    }

    private IDLInt(byte b, char c) {
        super(b, c);
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