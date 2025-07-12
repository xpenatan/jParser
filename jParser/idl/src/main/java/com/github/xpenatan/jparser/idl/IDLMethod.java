package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLMethod {
    public final IDLFile idlFile;
    public final IDLClass idlClass;

    public IDLLine idlLine;
    public String paramsLine;
    public String returnType;
    public IDLClassOrEnum returnClassType;
    public String name;
    public boolean isReturnArray;
    public boolean skip = false;
    public boolean isAny = false;
    public boolean isReturnRef;
    public boolean isReturnValue;
    public boolean isReturnConst;
    public boolean isReturnNewObject;
    public boolean isReturnMemoryOwned;
    public boolean isStaticMethod = false;
    public String operator = "";
    public String bindsToName = null;

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLMethod(IDLClass idlClass, IDLFile idlFile) {
        this.idlClass = idlClass;
        this.idlFile = idlFile;
    }

    public void initMethod(IDLLine idlLine) {
        this.idlLine = idlLine;
        String line = idlLine.line;
        paramsLine = IDLMethod.setParameters(idlFile, line, parameters);
        for(IDLParameter parameter : parameters) {
            parameter.idlMethod = this;
        }
        int index = line.indexOf("(");
        String leftSide = line.substring(0, index).trim();
        leftSide = IDLHelper.removeMultipleSpaces(leftSide.trim());

        isReturnNewObject = idlLine.containsCommand(IDLLine.CMD_NEW_OBJECT);
        isReturnMemoryOwned = !idlLine.containsCommand(IDLLine.CMD_NOT_MEM_OWN);
        skip = idlLine.containsCommand(IDLLine.CMD_SKIP);

        String tagsStr = IDLHelper.getTags(leftSide);
        if(!tagsStr.isEmpty()) {
            leftSide = leftSide.replace(tagsStr, "").trim();
            tagsStr = tagsStr.substring(1, tagsStr.length()-1);
            for(String s : tagsStr.split(",")) {
                s = s.trim();
                if(s.contains("Operator")) {
                    int first = s.indexOf("\"");
                    int last = s.lastIndexOf("\"");
                    operator = s.substring(first, last + 1).replace("\"", "");
                }
                else if(s.equals("Ref")) {
                    isReturnRef = true;
                }
                else if(s.equals("Value")) {
                    isReturnValue = true;
                }
                else if(s.equals("Const")) {
                    isReturnConst = true;
                }
                else if(s.startsWith("BindTo")) {
                    s = s.replace("\"", "");
                    bindsToName = s.split("=")[1];
                }
            }
        }

        if(leftSide.contains("[]")) {
            leftSide = leftSide.replace("[]", "").trim();
            isReturnArray = true;
        }

        if(leftSide.contains("static")) {
            leftSide = leftSide.replace("static", "").trim();
            isStaticMethod = true;
        }

        String[] s1 = leftSide.split(" ");
        name = s1[s1.length-1];

        returnType = "";
        int sss = s1.length - 1;
        for(int i = 0; i < sss; i++) {
            returnType += s1[i];
            if(i < sss-1) {
                returnType += " ";
            }
        }

        if(isReturnArray) {
            returnType = returnType + "[]";
        }

        if(returnType.contains("any") || returnType.contains("VoidPtr")) {
            isAny = true;
        }
    }

    public String getCPPReturnType() {
        String fullType = returnType;
        if(returnClassType != null && returnClassType.isClass()) {
            IDLClass aClass = returnClassType.asClass();
            fullType = aClass.getCPPName();
        }
        return IDLHelper.getCPPReturnType(fullType);
    }

    public String getJavaReturnType() {
        return IDLHelper.getJavaType(returnType);
    }

    public int getTotalOptionalParams() {
        int count = 0;
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            if(parameter.line.contains("optional")) {
                count++;
            }
        }
        return count;
    }

    public void removeLastParam(int count) {
        for(int i = 0; i < count; i++) {
            parameters.remove(parameters.size() - 1);
        }
    }

    public boolean nameEquals(String value) {
        if(bindsToName != null) {
            if(value.equals(bindsToName)) {
                return true;
            }
        }
        else {
            return value.equals(name);
        }
        return false;
    }

    public String getCPPName() {
        if(bindsToName != null) {
            return bindsToName;
        }
        return name;
    }

    public IDLMethod clone() {
        IDLMethod cloned = new IDLMethod(idlClass, idlFile);
        cloned.idlLine = idlLine.copy();
        cloned.paramsLine = paramsLine;
        cloned.returnType = returnType;
        cloned.name = name;
        cloned.skip = skip;
        cloned.isAny = isAny;
        cloned.bindsToName = bindsToName;
        cloned.isReturnValue = isReturnValue;
        cloned.isReturnArray = isReturnArray;
        cloned.isStaticMethod = isStaticMethod;
        cloned.isReturnRef = isReturnRef;

        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            IDLParameter clonedParam = parameter.clone();
            cloned.parameters.add(clonedParam);
        }
        return cloned;
    }

    static String setParameters(IDLFile idlFile, String line, ArrayList<IDLParameter> out) {
        int firstIdx = line.indexOf("(");
        int lastIdx = line.indexOf(")");
        String params = line.substring(firstIdx, lastIdx + 1);
        params = params.replace("(", "").replace(")", "");
        if(!params.isEmpty()) {
            String[] paramSplit = params.split(",");
            String cur = "";
            for(int i = 0; i < paramSplit.length; i++) {
                String text = paramSplit[i];
                cur = cur + text;

                boolean containsTags = text.contains("[") || text.contains("]");
                boolean isArray = text.contains("[]");

                if(!containsTags || isArray) {
                    IDLParameter parameter = new IDLParameter(idlFile);
                    parameter.initParameter(cur.trim());
                    out.add(parameter);
                    cur = "";
                }
                else {
                    // Small logic to keep getting the param if it contains [Const, Ref], [Const] or [Ref]
                    if(text.contains("]")) {
                        IDLParameter parameter = new IDLParameter(idlFile);
                        parameter.initParameter(cur.trim());
                        out.add(parameter);
                        cur = "";
                    }
                }
            }
            return params;
        }
        else {
            return null;
        }
    }
}