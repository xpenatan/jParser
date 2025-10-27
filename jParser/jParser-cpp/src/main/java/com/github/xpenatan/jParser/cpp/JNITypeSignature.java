package com.github.xpenatan.jParser.cpp;

public enum JNITypeSignature {
    Void("V"), Boolean("Z"), Byte("B"), Char("C"), Short("S"), Int("I"), Long("J"), Float("F"), Double("D"), String("Ljava/lang/String;");

    private final String jniType;

    JNITypeSignature(String type) {
        jniType = type;
    }

    String getJNIType() {
        return jniType;
    }

    static String getJNIType(String type) {
        JNITypeSignature[] values = JNITypeSignature.values();
        for(int i = 0; i < values.length; i++) {
            JNITypeSignature value = values[i];
            if(value.name().toLowerCase().equals(type)) {
                return value.getJNIType();
            }
        }
        return null;
    }

    static String getJNIObject(String classpath) {
        classpath = classpath.replace(".", "/");
        return "L"+ classpath + ";";
    }
}