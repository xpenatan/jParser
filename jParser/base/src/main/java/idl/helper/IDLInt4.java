package idl.helper;

public class IDLInt4 extends IDLIntArray {

    public static IDLInt4 TMP_1 = new IDLInt4();
    public static IDLInt4 TMP_2 = new IDLInt4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLInt4() {
        super(4);
    }

    IDLInt4 set(int value0, int value1, int value2, int value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    IDLInt4 set0(int value) {
        setValue(0, value);
        return this;
    }

    IDLInt4 set1(int value) {
        setValue(1, value);
        return this;
    }

    IDLInt4 set2(int value) {
        setValue(2, value);
        return this;
    }

    IDLInt4 set3(int value) {
        setValue(3, value);
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

    int get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}