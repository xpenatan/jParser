package com.github.xpenatan.jparser.core.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLMethod {
    public final IDLFile idlFile;

    public String line;
    public String paramsLine;
    public String returnType;
    public String name;
    public boolean returnIsArray;
    public boolean skip = false;
    public boolean isReturnRef;

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLMethod(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initMethod(String line) {
        this.line = line;
        paramsLine = IDLMethod.setParameters(idlFile, line, parameters);
        int index = line.indexOf("(");
        String leftSide = line.substring(0, index).trim();
        String returnInfo = "";
        if(leftSide.startsWith("[")) {
            int i = leftSide.indexOf("]") + 1;
            returnInfo = line.substring(0, i);
        }

        isReturnRef = returnInfo.contains("Ref");

        int endIndex = getLastIndex(leftSide);
        if(endIndex != -1) {
            leftSide = line.substring(endIndex + 1, index).trim();
        }

        if(leftSide.contains("[]")) {
            leftSide = leftSide.replace("[]", "");
            returnIsArray = true;
        }
        leftSide = leftSide.trim().replaceAll(" +", " ");
        String[] s = leftSide.split(" ");
        returnType = s[0];
        if(returnType.equals("long")) {
            returnType = "int";
        }
        name = s[1];

        if(paramsLine != null && paramsLine.contains("any ")) {
            skip = true;
        }
        if(returnType.contains("any")) {
            skip = true;
        }
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
        IDLMethod clonedMethod = new IDLMethod(idlFile);
        clonedMethod.line = line;
        clonedMethod.paramsLine = paramsLine;
        clonedMethod.returnType = returnType;
        clonedMethod.name = name;
        clonedMethod.skip = skip;
        clonedMethod.isReturnRef = isReturnRef;

        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            IDLParameter clonedParam = parameter.clone();
            clonedMethod.parameters.add(clonedParam);
        }
        return clonedMethod;
    }

    private int getLastIndex(String leftSide) {
        int startIndex = leftSide.indexOf("[");
        if(startIndex != -1) {
            int count = 0;
            for(int i = startIndex; i < leftSide.length(); i++) {
                char c = leftSide.charAt(i);
                if(c == '[') {
                    count++;
                }
                else if(c == ']') {
                    count--;
                }
                if(count == 0) {
                    return i;
                }
            }
        }
        return -1;
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