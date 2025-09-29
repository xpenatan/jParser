package idl.helper;

public class IDLFloat2 extends IDLFloatArray {

    public static final IDLFloat2 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLFloat2 native_new() {
        return new IDLFloat2((byte) 1, (char) 1);
    }

    private IDLFloat2(byte b, char c) {
        super(b, c);
    }

    public IDLFloat2() {
        super(2);
    }

    public IDLFloat2 set(float value0, float value1) {
        setValue(0, value0);
        setValue(1, value1);
        return this;
    }

    public IDLFloat2 set0(float value) {
        setValue(0, value);
        return this;
    }

    public IDLFloat2 set1(float value) {
        setValue(1, value);
        return this;
    }

    public float get0() {
        return getValue(0);
    }

    public float get1() {
        return getValue(1);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1();
    }
}