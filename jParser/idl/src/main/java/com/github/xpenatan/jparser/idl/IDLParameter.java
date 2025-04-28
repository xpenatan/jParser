package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLParameter {
    public final IDLFile idlFile;
    public IDLMethod idlMethod;
    public IDLConstructor idlConstructor;
    public IDLClassOrEnum idlClassOrEnum;

    public String line;
    public String idlType;
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

    public IDLParameter(IDLAttribute idlAttribute) {
        this.idlFile = idlAttribute.idlFile;
        idlType = idlAttribute.idlType;
        name = idlAttribute.name;
        isArray = idlAttribute.isArray;
        isRef = false;
        isConst = idlAttribute.isConst;
        isValue = idlAttribute.isValue;
        isAny = idlAttribute.isAny;
        idlClassOrEnum = idlAttribute.idlClassOrEnum;
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

        idlType = "";
        int sss = s1.length - 1;
        for(int i = 0; i < sss; i++) {
            idlType += s1[i];
            if(i < sss-1) {
                idlType += " ";
            }
        }

        if(isArray) {
            idlType = idlType + "[]";
        }

        if(idlType.equals("any") || idlType.equals("VoidPtr")) {
            isAny = true;
        }
    }

    public String getCPPType() {
        String fullType = idlType;
        if(idlClassOrEnum != null && idlClassOrEnum.isClass()) {
            IDLClass aClass = idlClassOrEnum.asClass();
            fullType = aClass.getCPPName();
            if(isArray && !fullType.endsWith("[]")) {
                fullType += "[]";
            }
        }
        return IDLHelper.getCPPReturnType(fullType);
    }

    public String getJavaType() {
        return IDLHelper.getJavaType(idlType);
    }

    public boolean isEnum() {
        return idlClassOrEnum != null && idlClassOrEnum.isEnum();
    }

    public IDLParameter clone() {
        IDLParameter clonedParam = new IDLParameter(idlFile);
        clonedParam.line = line;
        clonedParam.idlType = idlType;
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