package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLConstructor {
    public final IDLFile idlFile;

    public IDLClass idlClass;
    public String line;
    public String paramsLine;

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLConstructor(IDLFile idlFile, IDLClass idlClass) {
        this.idlFile = idlFile;
        this.idlClass = idlClass;
    }

    public void initConstructor(String line) {
        this.line = line;
        paramsLine = IDLMethod.setParameters(idlFile, line, parameters);
        for(IDLParameter parameter : parameters) {
            parameter.idlConstructor = this;
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

    public IDLConstructor clone() {
        IDLConstructor cloned = new IDLConstructor(idlFile, idlClass);
        cloned.line = line;
        cloned.paramsLine = paramsLine;
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            IDLParameter clonedParam = parameter.clone();
            cloned.parameters.add(clonedParam);
        }
        return cloned;
    }
}