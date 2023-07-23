package com.github.xpenatan.jparser.teavm;

import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;

public class TeaVMCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

    private final String module;

    public TeaVMCodeParserV2(IDLReader idlReader, String module, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.module = module;
        generateClass = true;
    }
}