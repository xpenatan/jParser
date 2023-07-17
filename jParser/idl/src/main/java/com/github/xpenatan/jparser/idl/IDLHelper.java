package com.github.xpenatan.jparser.idl;

public class IDLHelper {
    public static String removeMultipleSpaces(String in) {
        return in.replaceAll(" +", " ");
    }
}
