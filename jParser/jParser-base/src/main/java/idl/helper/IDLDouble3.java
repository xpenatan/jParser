package idl.helper;

public class IDLDouble3 extends IDLDoubleArray {

    public static final IDLDouble3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLDouble3 native_new() {
        return new IDLDouble3((byte) 1, (char) 1);
    }

    private IDLDouble3(byte b, char c) {
        super(b, c);
    }

    public IDLDouble3() {
        super(3);
    }

    public IDLDouble3 set(double value0, double value1, double value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    public IDLDouble3 set0(double value) {
        setValue(0, value);
        return this;
    }

    public IDLDouble3 set1(double value) {
        setValue(1, value);
        return this;
    }

    public IDLDouble3 set2(double value) {
        setValue(2, value);
        return this;
    }

    public double get0() {
        return getValue(0);
    }

    public double get1() {
        return getValue(1);
    }

    public double get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}