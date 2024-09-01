package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLParameter {
    public final IDLFile idlFile;

    public String line;
    public String type;
    public String name;
    public boolean isArray;
    public boolean isRef;
    public boolean isConst;
    public boolean isValue;
    public boolean isAny;
    public final ArrayList<String> tags = new ArrayList<>();

    public boolean optional;

    public IDLParameter(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initParameter(String line) {
        line = IDLHelper.removeMultipleSpaces(line);
        this.line = line;
        String tmpLine = line;

        optional = line.contains("optional");
        isArray = line.contains("[]");
        tmpLine = tmpLine.replace("optional", "").trim();
        tmpLine = tmpLine.replace("[]", "").trim();

        int startIndex = tmpLine.indexOf("[");
        int endIndex = tmpLine.indexOf("]");
        if(startIndex != -1 && endIndex != -1 && startIndex + 2 < endIndex) {
            String tagsStr = tmpLine.substring(startIndex, endIndex + 1);
            isRef = tagsStr.contains("Ref");
            isConst = tagsStr.contains("Const");
            isValue = tagsStr.contains("Value");
            String substring2 = tagsStr.replace("[", "").replace("]", "");
            String[] s = substring2.split(" ");
            for(int i = 0; i < s.length; i++) {
                String tag = s[i];
                tags.add(tag);
            }
            tmpLine = tmpLine.replace(tagsStr, "").trim();
        }

        if(isArray) {
            tmpLine = IDLHelper.removeMultipleSpaces(tmpLine.replace("[]", ""));
        }

        String[] s1 = tmpLine.split(" ");
        name = s1[s1.length - 1];

        type = "";
        int sss = s1.length - 1;
        for(int i = 0; i < sss; i++) {
            type += s1[i];
            if(i < sss-1) {
                type += " ";
            }
        }

        if(isArray) {
            type = type + "[]";
        }

        if(type.contains("long long")) {
            type = type.replace("long long", "long");
        }
        else if(type.contains("long")) {
            // long in webidl means int
            type = type.replace("long", "int");
        }

        if(type.equals("any") || type.equals("VoidPtr")) {
            type = "void*";
            isAny = true;
        }

        if(type.equals("DOMString")) {
            type = "String";
        }
        if(type.equals("octet")) {
            type = "byte";
        }

        // Convert to array object
        if(type.equals("int[]")) {
            type = "IDLIntArray";
        }
        else if(type.equals("float[]")) {
            type = "IDLFloatArray";
        }
        else if(type.equals("byte[]")) {
            type = "IDLByteArray";
        }
        else if(type.equals("boolean[]")) {
            type = "IDLBoolArray";
        }
        else if(type.equals("double[]")) {
            type = "IDLDoubleArray";
        }
    }

    public String getType() {
        if(isAny) {
            return "long";
        }
        return type.replace("unsigned", "").trim();
    }


    public IDLParameter clone() {
        IDLParameter clonedParam = new IDLParameter(idlFile);
        clonedParam.line = line;
        clonedParam.type = type;
        clonedParam.name = name;
        clonedParam.isRef = isRef;
        clonedParam.isAny = isAny;
        clonedParam.isArray = isArray;
        clonedParam.isValue = isValue;
        clonedParam.isConst = isConst;
        clonedParam.tags.addAll(tags);
        return clonedParam;
    }
}