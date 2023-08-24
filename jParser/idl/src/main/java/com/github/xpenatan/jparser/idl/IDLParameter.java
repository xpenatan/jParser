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

        int startIndex = line.indexOf("[");
        int endIndex = line.indexOf("]");
        if(startIndex != -1 && endIndex != -1 && startIndex + 2 < endIndex) {
            String tagsStr = line.substring(startIndex, endIndex + 1);
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
        type = s1[s1.length - 2];

        if(isArray) {
            type = type + "[]";
        }

        if(type.equals("long")) {
            type = "int";
        }

        if(type.equals("long[]")) {
            type = "int[]";
        }

        if(type.equals("DOMString")) {
            type = "String";
        }
        name = s1[s1.length - 1];
    }

    public IDLParameter clone() {
        IDLParameter clonedParam = new IDLParameter(idlFile);
        clonedParam.line = line;
        clonedParam.type = type;
        clonedParam.name = name;
        clonedParam.isRef = isRef;
        clonedParam.isConst = isConst;
        clonedParam.tags.addAll(tags);
        return clonedParam;
    }
}