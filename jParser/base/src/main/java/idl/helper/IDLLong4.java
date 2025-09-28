package idl.helper;

public class IDLLong4 extends IDLLongArray {

    public static final IDLLong4 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong4 native_new() {
        return new IDLLong4((byte) 1, (char) 1);
    }

    private IDLLong4(byte b, char c) {
        super(b, c);
    }


    public IDLLong4() {
        super(4);
    }

    public IDLLong4 set(long value0, long value1, long value2, long value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    public IDLLong4 set0(long value) {
        setValue(0, value);
        return this;
    }

    public IDLLong4 set1(long value) {
        setValue(1, value);
        return this;
    }

    public IDLLong4 set2(long value) {
        setValue(2, value);
        return this;
    }

    public IDLLong4 set3(long value) {
        setValue(3, value);
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

    public long get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}