package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String path = "src\\main\\resources\\idl\\Test.idl";

        String basePath = new File(".").getAbsolutePath() + "./example-base/src";

        String genDir = "../example-core/src";

        String basePackage = "com.github.xpenatan.jparser.example";

        IDLReader idlReader = IDLReader.readIDL(path);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        JParser.generate(idlParser, basePath, genDir);
    }
}
