package com.github.xpenatan.jparser.core.codeparser;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLReader;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    protected final IDLReader idlReader;

    public IDLClassGeneratorParser(String headerCMD, IDLReader idlReader) {
        super(headerCMD);
        this.idlReader = idlReader;
    }

    @Override
    public void onParseStart(JParser jParser) {

//        jParser.

        System.out.println();
    }
}