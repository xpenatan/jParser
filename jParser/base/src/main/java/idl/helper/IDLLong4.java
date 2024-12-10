package idl.helper;

public class IDLLong4 extends IDLLongArray {

    public static IDLLong4 TMP_1 = new IDLLong4();
    public static IDLLong4 TMP_2 = new IDLLong4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
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