package com.github.xpenatan.jparser.teavm;

import com.github.xpenatan.jparser.core.codeparser.idl.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;

public class TeaVMCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

    private final String module;

    public TeaVMCodeParserV2(String module, IDLReader idlReader) {
        super(HEADER_CMD, idlReader);
        this.module = module;
    }

    public TeaVMCodeParserV2(String basePackage, String module, IDLReader idlReader) {
        super(basePackage, HEADER_CMD, idlReader);
        this.module = module;
    }
}