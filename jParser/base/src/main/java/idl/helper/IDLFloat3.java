package idl.helper;

public class IDLFloat3 extends IDLFloatArray {

    public static final IDLFloat3 NULL = createInstance();
    public static IDLFloat3 TMP_1 = new IDLFloat3();
    public static IDLFloat3 TMP_2 = new IDLFloat3();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat3 createInstance() {
        return new IDLFloat3((byte) 1, (char) 1);
    }

    private IDLFloat3(byte b, char c) {
        super(b, c);
    }

    public IDLFloat3() {
        super(3);
    }

    public IDLFloat3 set(float value0, float value1, float value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    public IDLFloat3 set0(float value) {
        setValue(0, value);
        return this;
    }

    public IDLFloat3 set1(float value) {
        setValue(1, value);
        return this;
    }

    public IDLFloat3 set2(float value) {
        setValue(2, value);
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

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}