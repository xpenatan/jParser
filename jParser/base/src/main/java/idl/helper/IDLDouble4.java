package idl.helper;

public class IDLDouble4 extends IDLDoubleArray {

    public static IDLDouble4 TMP_1 = new IDLDouble4();
    public static IDLDouble4 TMP_2 = new IDLDouble4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLDouble4() {
        super(4);
    }

    public IDLDouble4 set(double value0, double value1, double value2, double value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    public IDLDouble4 set0(double value) {
        setValue(0, value);
        return this;
    }

    public IDLDouble4 set1(double value) {
        setValue(1, value);
        return this;
    }

    public IDLDouble4 set2(double value) {
        setValue(2, value);
        return this;
    }

    public IDLDouble4 set3(double value) {
        setValue(3, value);
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

    public double get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}