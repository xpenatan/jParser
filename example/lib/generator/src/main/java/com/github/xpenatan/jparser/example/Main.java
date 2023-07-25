package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.cpp.CPPBuildHelper;
import com.github.xpenatan.jparser.cpp.CppCodeParserV2;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.FileCopyHelper;
import com.github.xpenatan.jparser.cpp.NativeCPPGeneratorV2;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.teavm.TeaVMCodeParserV2;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
//        generateClassOnly();
        generate();
    }

    private static void generateClassOnly() throws Exception {
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "cpp\\exampleLib.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String genDir = "../core/src/main/java";

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generate() throws Exception {
        String libName = "exampleLib";
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "cpp\\exampleLib.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";

        IDLReader idlReader = IDLReader.readIDL(idlPath);

        generateCPP(idlReader, libName, basePackage, baseJavaDir);
        generateTeaVM(idlReader, libName, basePackage, baseJavaDir);
    }

    private static void generateCPP(
            IDLReader idlReader,
            String libName,
            String basePackage,
            String baseJavaDir
    ) throws Exception {
        String libsDir = new File("./build/c++/desktop/").getCanonicalPath();
        String genDir = "../core/src/main/java";
        String libBuildPath = new File("./build/c++/").getCanonicalPath();
        String libSourcePath = libBuildPath + "/src";

        FileCopyHelper.copyDir( "./cpp/exampleLib/src/", libSourcePath);

        CppGenerator cppGenerator = new NativeCPPGeneratorV2(libSourcePath);
        CppCodeParserV2 cppParser = new CppCodeParserV2(cppGenerator, idlReader, basePackage);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, genDir);
        CPPBuildHelper.DEBUG_BUILD = true;
        CPPBuildHelper.build(libName, libBuildPath, libsDir);
    }

    public static void generateTeaVM(IDLReader idlReader, String libName, String basePackage, String baseJavaDir) throws Exception {
        String genDir = "../teavm/src/main/java/emu/";
        TeaVMCodeParserV2 teavmParser = new TeaVMCodeParserV2(idlReader, libName, basePackage);
        JParser.generate(teavmParser, baseJavaDir, genDir);
    }
}
