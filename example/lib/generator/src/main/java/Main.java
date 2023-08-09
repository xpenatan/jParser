import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.cpp.CPPBuildHelper;
import com.github.xpenatan.jparser.cpp.CppCodeParserV2;
import com.github.xpenatan.jparser.cpp.CppGenerator;
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
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String idlPath = "src\\main\\cpp\\exampleLib.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String genDir = "../core/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/exampleLib/src/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath, cppSourceDir);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generate() throws Exception {
        String libName = "exampleLib";
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String idlPath = "src\\main\\cpp\\exampleLib.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/exampleLib/src/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath, cppSourceDir);

        generateCPP(idlReader, libName, basePackage, baseJavaDir, cppSourceDir);
        generateTeaVM(idlReader, libName, basePackage, baseJavaDir);
    }

    private static void generateCPP(
            IDLReader idlReader,
            String libName,
            String basePackage,
            String baseJavaDir,
            String cppSourceDir
    ) throws Exception {
        String libsDir = new File("./build/c++/desktop/").getCanonicalPath();
        String genDir = "../core/src/main/java";
        String libBuildPath = new File("./build/c++/").getCanonicalPath();
        String cppDestinationPath = libBuildPath + "/src";

        CppGenerator cppGenerator = new NativeCPPGeneratorV2(cppSourceDir, cppDestinationPath);
        CppCodeParserV2 cppParser = new CppCodeParserV2(cppGenerator, idlReader, basePackage);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, genDir);
        CPPBuildHelper.DEBUG_BUILD = true;
        CPPBuildHelper.Config config = new CPPBuildHelper.Config();
        config.libName = libName;
        config.buildPath = libBuildPath;
        config.libsDir = libsDir;
        CPPBuildHelper.build(config);
    }

    public static void generateTeaVM(IDLReader idlReader, String libName, String basePackage, String baseJavaDir) throws Exception {
        String genDir = "../teavm/src/main/java/";
        TeaVMCodeParserV2 teavmParser = new TeaVMCodeParserV2(idlReader, libName, basePackage);
        JParser.generate(teavmParser, baseJavaDir, genDir);
    }
}
