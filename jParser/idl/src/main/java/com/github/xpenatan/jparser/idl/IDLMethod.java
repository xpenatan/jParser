package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLMethod {
    public final IDLFile idlFile;
    public final IDLClass idlClass;

    public String line;
    public String paramsLine;
    public String returnType;
    public String name;
    public boolean isReturnArray;
    public boolean skip = false;
    public boolean isAny = false;
    public boolean isReturnRef;
    public boolean isReturnValue;
    public boolean isReturnConst;
    public boolean isStaticMethod = false;
    public String operator = "";

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLMethod(IDLClass idlClass, IDLFile idlFile) {
        this.idlClass = idlClass;
        this.idlFile = idlFile;
    }

    public void initMethod(String line) {
        this.line = line;
        paramsLine = IDLMethod.setParameters(idlFile, line, parameters);
        int index = line.indexOf("(");
        String leftSide = line.substring(0, index).trim();
        leftSide = IDLHelper.removeMultipleSpaces(leftSide.trim());

        String tagsStr = IDLHelper.getTags(leftSide);
        if(!tagsStr.isEmpty()) {
            isReturnRef = tagsStr.contains("Ref");
            isReturnValue = tagsStr.contains("Value");
            isReturnConst = tagsStr.contains("Const");
            leftSide = leftSide.replace(tagsStr, "").trim();

            tagsStr = tagsStr.substring(1, tagsStr.length()-1);
            for(String s : tagsStr.split(",")) {
                if(s.contains("Operator")) {
                    int first = s.indexOf("\"");
                    int last = s.lastIndexOf("\"");
                    operator = s.substring(first, last + 1).replace("\"", "");
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

        if(returnType.contains("long long")) {
            returnType = returnType.replace("long long", "long");
        }
        else if(returnType.contains("long")) {
            returnType = returnType.replace("long", "int");
        }
        if(returnType.equals("DOMString")) {
            returnType = "String";
        }

        if(returnType.contains("any") || returnType.contains("VoidPtr")) {
            isAny = true;
            returnType = "long";
        }
    }

    public String getCPPReturnType() {
        return returnType;
    }

    public String getJavaReturnType() {
        return returnType.replace("unsigned", "").trim();
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

    public IDLMethod clone() {
        IDLMethod cloned = new IDLMethod(idlClass, idlFile);
        cloned.line = line;
        cloned.paramsLine = paramsLine;
        cloned.returnType = returnType;
        cloned.name = name;
        cloned.skip = skip;
        cloned.isAny = isAny;
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