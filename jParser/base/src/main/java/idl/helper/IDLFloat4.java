package idl.helper;

public class IDLFloat4 extends IDLFloatArray {

    public static IDLFloat4 TMP_1 = new IDLFloat4();
    public static IDLFloat4 TMP_2 = new IDLFloat4();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    public IDLFloat4() {
        super(4);
    }

    public IDLFloat4 set(float value0, float value1, float value2, float value3) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        setValue(3, value3);
        return this;
    }

    public IDLFloat4 set0(float value) {
        setValue(0, value);
        return this;
    }

    public IDLFloat4 set1(float value) {
        setValue(1, value);
        return this;
    }

    public IDLFloat4 set2(float value) {
        setValue(2, value);
        return this;
    }

    public IDLFloat4 set3(float value) {
        setValue(3, value);
        return this;
    }

    public float get0() {
        return getValue(0);
    }

    public float get1() {
        return getValue(1);
    }

    public float get2() {
        return getValue(2);
    }

    public float get3() {
        return getValue(3);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2() + ", " + get3();
    }
}