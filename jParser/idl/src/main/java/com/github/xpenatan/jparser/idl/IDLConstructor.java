package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLConstructor {
    public final IDLFile idlFile;

    public String line;
    public String paramsLine;

    public final ArrayList<IDLParameter> parameters = new ArrayList<>();

    public IDLConstructor(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initConstructor(String line) {
        this.line = line;
        paramsLine = IDLMethod.setParameters(idlFile, line, parameters);
    }
}