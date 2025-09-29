package com.github.xpenatan.jparser.idl.parser.data;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;

public class IDLMethodData {
    public IDLMethod idlMethod;
    public MethodDeclaration methodDeclaration;

    public ArrayList<IDLParameterData> getParameters() {

        return null;
    }
}
