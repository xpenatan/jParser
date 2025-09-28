package idl.helper;

public class IDLInt3 extends IDLIntArray {

    public static final IDLInt3 NULL = native_new();

    /**
     * @return An empty instance without a native address
     */
    public static IDLInt3 native_new() {
        return new IDLInt3((byte) 1, (char) 1);
    }

    private IDLInt3(byte b, char c) {
        super(b, c);
    }

    public IDLInt3() {
        super(3);
    }

    public  IDLInt3 set(int value0, int value1, int value2) {
        setValue(0, value0);
        setValue(1, value1);
        setValue(2, value2);
        return this;
    }

    public IDLInt3 set0(int value) {
        setValue(0, value);
        return this;
    }

    public IDLInt3 set1(int value) {
        setValue(1, value);
        return this;
    }

    public IDLInt3 set2(int value) {
        setValue(2, value);
        return this;
    }

    public int get0() {
        return getValue(0);
    }

    public int get1() {
        return getValue(1);
    }

    public int get2() {
        return getValue(2);
    }

    @Override
    public String toString() {
        return get0() + ", " + get1() + ", " + get2();
    }
}