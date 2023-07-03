package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String path = "src\\main\\resources\\idl\\Test.idl";

        String bulletPath = new File("build\\gen").getCanonicalPath();
        String genDir = bulletPath;

        IDLFile idlFile = IDLReader.parseFile(path);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser("IDL-Test", idlFile);
        JParser.generate(idlParser, null, genDir);
    }
}
