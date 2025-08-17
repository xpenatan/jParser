package idl.helper;

public class IDLInt4 extends IDLIntArray {

    public static final IDLInt4 NULL = createInstance();
    public static IDLInt4 TMP_1 = new IDLInt4();
    public static IDLInt4 TMP_2 = new IDLInt4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt4 createInstance() {
        return new IDLInt4((byte) 1, (char) 1);
    }

    private IDLInt4(byte b, char c) {
        super(b, c);
    }

    public IDLInt4() {
        super(4);
    }

    public IDLInt4 set(int value0, int value1, int value2, int value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    public IDLInt4 set0(int value) {
        setValue(0, value);
        return this;
    }

    public IDLInt4 set1(int value) {
        setValue(1, value);
        return this;
    }

    public IDLInt4 set2(int value) {
        setValue(2, value);
        return this;
    }

    public IDLInt4 set3(int value) {
        setValue(3, value);
        return this;
    }

    public int get0() {
        return getValue(0);
    }

    public int get1() {
        return getValue(1);
    }

    public int get2() {
        return getValue(2);
    }

    public int get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}