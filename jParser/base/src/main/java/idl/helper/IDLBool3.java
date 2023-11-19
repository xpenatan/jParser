package idl.helper;

public class IDLBool3 extends IDLBoolArray {

    public static IDLBool3 TMP_1 = new IDLBool3();
    public static IDLBool3 TMP_2 = new IDLBool3();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLBool3() {
        super(3);
    }

    IDLBool3 set(boolean value0, boolean value1, boolean value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    IDLBool3 set0(boolean value) {
        setValue(0, value);
        return this;
    }

    IDLBool3 set1(boolean value) {
        setValue(1, value);
        return this;
    }

    IDLBool3 set2(boolean value) {
        setValue(2, value);
        return this;
    }

    boolean get0() {
        return getValue(0);
    }

    boolean get1() {
        return getValue(1);
    }

    boolean get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}