package idl.helper;

public class IDLInt2 extends IDLIntArray {

    public static final IDLInt2 NULL = native_new();
    public static IDLInt2 TMP_1 = new IDLInt2();
    public static IDLInt2 TMP_2 = new IDLInt2();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt2 native_new() {
        return new IDLInt2((byte) 1, (char) 1);
    }

    private IDLInt2(byte b, char c) {
        super(b, c);
    }

    public IDLInt2() {
        super(2);
    }

    public IDLInt2 set(int value0, int value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    public IDLInt2 set0(int value) {
        setValue(0, value);
        return this;
    }

    public IDLInt2 set1(int value) {
        setValue(1, value);
        return this;
    }

    public int get0() {
        return getValue(0);
    }

    public int get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}