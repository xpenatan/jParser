package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.codeparser.idl.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.cpp.CPPBuildHelper;
import com.github.xpenatan.jparser.cpp.CppCodeParserV2;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.FileCopyHelper;
import com.github.xpenatan.jparser.cpp.NativeCPPGeneratorV2;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
//        generateClassOnly();
        generateCPP();
    }

    private static void generateClassOnly() throws Exception {
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "src\\main\\resources\\idl\\Test.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./example-base/src/main/java";
        String genDir = "../example-core/src/main/java";

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generateCPP() throws Exception {
        String libName = "example-test";
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "src\\main\\resources\\idl\\Test.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./example-base/src/main/java";
        String genDir = "../example-core/src/main/java";
        String libsDir = new File("../example-desktop/src/main/resources/").getCanonicalPath();
        String jniBuildPath = new File("./build/c++/").getCanonicalPath();

        FileCopyHelper.copyDir( "./jni/cpp/src/", jniBuildPath + "/src");

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        CppGenerator cppGenerator = new NativeCPPGeneratorV2(jniBuildPath);
        CppCodeParserV2 idlParser = new CppCodeParserV2(cppGenerator, idlReader, basePackage);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
        CPPBuildHelper.DEBUG_BUILD = true;
        CPPBuildHelper.build(libName, jniBuildPath, libsDir);
    }
}
