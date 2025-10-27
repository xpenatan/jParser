package com.github.xpenatan.jParser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLConstructor {
    public final IDLFile idlFile;

    public IDLClass idlClass;
    public IDLLine idlLine;
    public String paramsLine;

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLConstructor(IDLFile idlFile, IDLClass idlClass) {
        this.idlFile = idlFile;
        this.idlClass = idlClass;
    }

    public void initConstructor(IDLLine idlLine) {
        this.idlLine = idlLine;
        paramsLine = IDLMethod.setParameters(idlFile, idlLine, parameters);
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
        cloned.idlLine = idlLine;
        cloned.paramsLine = paramsLine;
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            IDLParameter clonedParam = parameter.clone();
            cloned.parameters.add(clonedParam);
        }
        return cloned;
    }
}