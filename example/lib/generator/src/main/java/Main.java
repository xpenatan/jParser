import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.builder.targets.AndroidTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jparser.builder.targets.WindowsTarget;
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
        generateAndBuild();
    }

    private static void generateClassOnly() throws Exception {
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String idlPath = "src\\main\\cpp\\emscripten\\exampleLib.idl";
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String genDir = "../core/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/exampleLib/src/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath, cppSourceDir);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generateAndBuild() throws Exception {
        String libName = "exampleLib";
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String emscriptenCustomCodePath = new File("src\\main\\cpp\\emscripten").getCanonicalPath();
        String idlPath = new File(emscriptenCustomCodePath + "\\exampleLib.idl").getCanonicalPath();
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/source/exampleLib/src/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath, cppSourceDir);

        String libsDir = new File("./build/c++/libs/").getCanonicalPath();
        String genDir = "../core/src/main/java";
        String libBuildPath = new File("./build/c++/").getCanonicalPath();
        String cppDestinationPath = libBuildPath + "/src";
        String libDestinationPath = cppDestinationPath + "/exampleLib";

        CppGenerator cppGenerator = new NativeCPPGeneratorV2(cppSourceDir, libDestinationPath);
        CppCodeParserV2 cppParser = new CppCodeParserV2(cppGenerator, idlReader, basePackage);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, genDir);
        CPPBuildHelper.DEBUG_BUILD = true;
        CPPBuildHelper.Config config = new CPPBuildHelper.Config();
        config.libName = libName;
        config.buildPath = libBuildPath;
        config.libsDir = libsDir;

        config.headerDir.add("src/");
        config.cppIncludes.add("src/**/*.cpp");
        config.cppIncludes.add("src/JNIGlue.cpp");

//        CPPBuildHelper.build(config);

        BuildConfig buildConfig = new BuildConfig(
                cppDestinationPath,
                libBuildPath,
                libsDir,
                libName,
                emscriptenCustomCodePath
        );

        WindowsTarget windowsTarget = new WindowsTarget();
        windowsTarget.headerDirs.add("-Isrc/exampleLib");
        windowsTarget.cppIncludes.add("**/src/**.cpp");

        String teaVMgenDir = "../teavm/src/main/java/";
        TeaVMCodeParserV2 teavmParser = new TeaVMCodeParserV2(idlReader, libName, basePackage);
        JParser.generate(teavmParser, baseJavaDir, teaVMgenDir);
        EmscriptenTarget teaVMTarget = new EmscriptenTarget(idlPath);
        teaVMTarget.headerDirs.add("-Isrc/exampleLib");
        teaVMTarget.headerDirs.add("-include src/jsglue/Include.h");
        teaVMTarget.headerDirs.add("-include src/jsglue/custom_glue.cpp");
        teaVMTarget.cppIncludes.add("**/src/exampleLib/**.cpp");
        teaVMTarget.cppIncludes.add("**/src/jsglue/glue.cpp");

        AndroidTarget androidTarget = new AndroidTarget();
        androidTarget.headerDirs.add("-Isrc/exampleLib");
        androidTarget.cppIncludes.add("**/src/**.cpp");

//        JBuilder.build(buildConfig, windowsTarget, teaVMTarget);
        JBuilder.build(buildConfig, androidTarget);
    }
}
