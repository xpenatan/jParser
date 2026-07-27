package com.github.xpenatan.jParser.builder.bundle;

public enum NativeBridge {
    JNI("jni"),
    FFM("ffm"),
    TEAVM_C("teavm-c"),
    WEB("web");

    private final String id;

    NativeBridge(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    static NativeBridge fromId(String id) {
        for(NativeBridge value : values()) {
            if(value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported native bridge: " + id);
    }
}
