package idl.helper;

public class IDLBool2 extends IDLBoolArray {

    public static final IDLBool2 NULL = native_new();
    public static IDLBool2 TMP_1 = new IDLBool2();
    public static IDLBool2 TMP_2 = new IDLBool2();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLBool2 native_new() {
        return new IDLBool2((byte) 1, (char) 1);
    }

    private IDLBool2(byte b, char c) {
        super(b, c);
    }

    public IDLBool2() {
        super(2);
    }

    public IDLBool2 set(boolean value0, boolean value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    public IDLBool2 set0(boolean value) {
        setValue(0, value);
        return this;
    }

    public IDLBool2 set1(boolean value) {
        setValue(1, value);
        return this;
    }

    public boolean get0() {
        return getValue(0);
    }

    public boolean get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}