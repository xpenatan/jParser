package idl.helper;

public class IDLInt3 extends IDLIntArray {

    public static IDLInt3 TMP_1 = new IDLInt3();
    public static IDLInt3 TMP_2 = new IDLInt3();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLInt3() {
        super(3);
    }

    IDLInt3 set(int value0, int value1, int value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    IDLInt3 set0(int value) {
        setValue(0, value);
        return this;
    }

    IDLInt3 set1(int value) {
        setValue(1, value);
        return this;
    }

    IDLInt3 set2(int value) {
        setValue(2, value);
        return this;
    }

    int get0() {
        return getValue(0);
    }

    int get1() {
        return getValue(1);
    }

    int get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}