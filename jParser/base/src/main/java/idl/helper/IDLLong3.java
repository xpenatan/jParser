package idl.helper;

public class IDLLong3 extends IDLLongArray {

    public static final IDLLong3 NULL = createInstance();
    public static IDLLong3 TMP_1 = new IDLLong3();
    public static IDLLong3 TMP_2 = new IDLLong3();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong3 createInstance() {
        return new IDLLong3((byte) 1, (char) 1);
    }

    private IDLLong3(byte b, char c) {
        super(b, c);
    }

    public IDLLong3() {
        super(3);
    }

    public IDLLong3 set(long value0, long value1, long value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    public IDLLong3 set0(long value) {
        setValue(0, value);
        return this;
    }

    public IDLLong3 set1(long value) {
        setValue(1, value);
        return this;
    }

    public IDLLong3 set2(long value) {
        setValue(2, value);
        return this;
    }

    public long get0() {
        return getValue(0);
    }

    public long get1() {
        return getValue(1);
    }

    public long get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}