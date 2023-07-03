package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "src\\main\\resources\\idl\\Test.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./example-base/src/main/java";
        String genDir = "../example-core/src/main/java";

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        JParser.generate(idlParser, baseJavaDir, genDir);
    }
}
