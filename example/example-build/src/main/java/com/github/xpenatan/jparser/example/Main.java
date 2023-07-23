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
        String idlPath = "src\\main\\resources\\idl\\Test.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./example-base/src/main/java";
        String genDir = "../example-core/src/main/java";

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generate() throws Exception {
        String libName = "example-test";
        String basePackage = "com.github.xpenatan.jparser.example";
        String idlPath = "src\\main\\resources\\idl\\Test.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./example-base/src/main/java";

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
        String libsDir = new File("../example-desktop/src/main/resources/").getCanonicalPath();
        String genDir = "../example-core/src/main/java";
        String jniBuildPath = new File("./build/c++/").getCanonicalPath();
        String jniSourcePath = jniBuildPath + "/src";

        FileCopyHelper.copyDir( "./jni/cpp/src/", jniSourcePath);

        CppGenerator cppGenerator = new NativeCPPGeneratorV2(jniSourcePath);
        CppCodeParserV2 cppParser = new CppCodeParserV2(cppGenerator, idlReader, basePackage);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, genDir);
        CPPBuildHelper.DEBUG_BUILD = true;
        CPPBuildHelper.build(libName, jniBuildPath, libsDir);
    }

    public static void generateTeaVM(IDLReader idlReader, String libName, String basePackage, String baseJavaDir) throws Exception {
        String genDir = "../example-teavm/src/main/java";
        TeaVMCodeParserV2 teavmParser = new TeaVMCodeParserV2(idlReader, libName, basePackage);
        JParser.generate(teavmParser, baseJavaDir, genDir);
    }
}
