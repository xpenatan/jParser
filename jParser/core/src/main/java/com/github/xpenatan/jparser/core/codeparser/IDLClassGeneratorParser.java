package com.github.xpenatan.jparser.core.codeparser;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLFile;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    protected final IDLFile idlFile;

    public IDLClassGeneratorParser(String headerCMD, IDLFile idlFile) {
        super(headerCMD);
        this.idlFile = idlFile;
    }

    @Override
    public void onParseStart(JParser jParser) {

    }
}