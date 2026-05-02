package com.github.xpenatan.jParser.idl.parser.data;

import com.github.javaparser.ast.body.Parameter;
import com.github.xpenatan.jParser.idl.IDLParameter;

public class IDLParameterData {
    public IDLParameter idlParameter;
    public Parameter parameter;

    public boolean isEnum() {
        if(idlParameter != null && idlParameter.idlClassOrEnum != null) {
            return idlParameter.idlClassOrEnum.isEnum();
        }
        return false;
    }

    public boolean isClass() {
        if(idlParameter != null) {
            return idlParameter.idlClassOrEnum.isEnum();
        }
        return false;
    }
}
