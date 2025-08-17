package idl.helper;

public class IDLLong extends IDLLongArray {

    public static final IDLLong NULL = createInstance();
    public static IDLLong TMP_1 = new IDLLong();
    public static IDLLong TMP_2 = new IDLLong();
    public static IDLLong TMP_3 = new IDLLong();
    public static IDLLong TMP_4 = new IDLLong();

    public static void disposeTEMP() {
        TMP_1.dispose();
        TMP_2.dispose();
        TMP_3.dispose();
        TMP_4.dispose();
    }

    /**
     * @return An empty instance without a native address
     */
    public static IDLLong createInstance() {
        return new IDLLong((byte) 1, (char) 1);
    }

    private IDLLong(byte b, char c) {
        super(b, c);
    }

    public IDLLong() {
        super(1);
    }

    public IDLLong(int value) {
        this();
        set(value);
    }

    public IDLLong set(long value) {
        setValue(0, value);
        return this;
    }

    public long getValue() {
        return getValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}