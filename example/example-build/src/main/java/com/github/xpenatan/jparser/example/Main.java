package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.cpp.CPPBuildHelper;
import com.github.xpenatan.jparser.cpp.CppCodeParser;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.FileCopyHelper;
import com.github.xpenatan.jparser.cpp.NativeCPPGenerator;
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
        String classPath = CppCodeParser.getClassPath("example-base", "gdx-1", "gdx-jnigen-loader", "jParser-loader");

        String cppPath = new File("./jni/").getCanonicalPath();
        String jniBuildPath = cppPath + "/build/c++/";
        FileCopyHelper.copyDir(cppPath + "/cpp/src/", jniBuildPath + "/src");

        IDLReader idlReader = IDLReader.readIDL(idlPath);

        CppGenerator cppGenerator = new NativeCPPGeneratorV2(jniBuildPath);
        CppCodeParser idlParser = new CppCodeParser(cppGenerator, idlReader, basePackage);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
//        CPPBuildHelper.build(libName, jniBuildPath);
    }
}
