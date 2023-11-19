package idl.helper;

public class IDLDouble3 extends IDLDoubleArray {

    public static IDLDouble3 TMP_1 = new IDLDouble3();
    public static IDLDouble3 TMP_2 = new IDLDouble3();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLDouble3() {
        super(3);
    }

    IDLDouble3 set(double value0, double value1, double value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    IDLDouble3 set0(double value) {
        setValue(0, value);
        return this;
    }

    IDLDouble3 set1(double value) {
        setValue(1, value);
        return this;
    }

    IDLDouble3 set2(double value) {
        setValue(2, value);
        return this;
    }

    double get0() {
        return getValue(0);
    }

    double get1() {
        return getValue(1);
    }

    double get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}