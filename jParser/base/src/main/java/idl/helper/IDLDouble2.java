package idl.helper;

public class IDLDouble2 extends IDLDoubleArray {

    public static IDLDouble2 TMP_1 = new IDLDouble2();
    public static IDLDouble2 TMP_2 = new IDLDouble2();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLDouble2() {
        super(2);
    }

    public IDLDouble2 set(double value0, double value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    public IDLDouble2 set0(double value) {
        setValue(0, value);
        return this;
    }

    public IDLDouble2 set1(double value) {
        setValue(1, value);
        return this;
    }

    public double get0() {
        return getValue(0);
    }

    public double get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}