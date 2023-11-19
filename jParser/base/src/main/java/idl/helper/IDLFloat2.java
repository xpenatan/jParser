package idl.helper;

public class IDLFloat2 extends IDLFloatArray {

    public static IDLFloat2 TMP_1 = new IDLFloat2();
    public static IDLFloat2 TMP_2 = new IDLFloat2();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLFloat2() {
        super(2);
    }

    IDLFloat2 set(float value0, float value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    IDLFloat2 set0(float value) {
        setValue(0, value);
        return this;
    }

    IDLFloat2 set1(float value) {
        setValue(1, value);
        return this;
    }

    float get0() {
        return getValue(0);
    }

    float get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}