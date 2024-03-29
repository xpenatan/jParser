package idl.helper;

public class IDLBool4 extends IDLBoolArray {

    public static IDLBool4 TMP_1 = new IDLBool4();
    public static IDLBool4 TMP_2 = new IDLBool4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLBool4() {
        super(4);
    }

    public IDLBool4 set(boolean value0, boolean value1, boolean value2, boolean value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    public IDLBool4 set0(boolean value) {
        setValue(0, value);
        return this;
    }

    public IDLBool4 set1(boolean value) {
        setValue(1, value);
        return this;
    }

    public IDLBool4 set2(boolean value) {
        setValue(2, value);
        return this;
    }

    public IDLBool4 set3(boolean value) {
        setValue(3, value);
        return this;
    }

    public boolean get0() {
        return getValue(0);
    }

    public boolean get1() {
        return getValue(1);
    }

    public boolean get2() {
        return getValue(2);
    }

    public boolean get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}