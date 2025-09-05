package idl.helper;

public class IDLLong2 extends IDLLongArray {

    public static final IDLLong2 NULL = native_new();
    public static IDLLong2 TMP_1 = new IDLLong2();
    public static IDLLong2 TMP_2 = new IDLLong2();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong2 native_new() {
        return new IDLLong2((byte) 1, (char) 1);
    }

    private IDLLong2(byte b, char c) {
        super(b, c);
    }

    public IDLLong2() {
        super(2);
    }

    public IDLLong2 set(long value0, long value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    public IDLLong2 set0(long value) {
        setValue(0, value);
        return this;
    }

    public IDLLong2 set1(long value) {
        setValue(1, value);
        return this;
    }

    public long get0() {
        return getValue(0);
    }

    public long get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}