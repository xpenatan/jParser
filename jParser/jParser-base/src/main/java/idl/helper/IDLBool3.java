package idl.helper;

public class IDLBool3 extends IDLBoolArray {

    public static final IDLBool3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLBool3 native_new() {
        return new IDLBool3((byte) 1, (char) 1);
    }

    private IDLBool3(byte b, char c) {
        super(b, c);
    }

    public IDLBool3() {
        super(3);
    }

    public IDLBool3 set(boolean value0, boolean value1, boolean value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    public IDLBool3 set0(boolean value) {
        setValue(0, value);
        return this;
    }

    public IDLBool3 set1(boolean value) {
        setValue(1, value);
        return this;
    }

    public IDLBool3 set2(boolean value) {
        setValue(2, value);
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

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}