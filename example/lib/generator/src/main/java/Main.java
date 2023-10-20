import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.builder.targets.AndroidTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenLibTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jparser.builder.targets.IOSTarget;
import com.github.xpenatan.jparser.builder.targets.WindowsTarget;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.util.FileHelper;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.cpp.CppCodeParser;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.teavm.TeaVMCodeParser;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
//        generateClassOnly();
        generateAndBuild();
    }

    private static void generateClassOnly() throws Exception {
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String idlPath = new File("src/main/cpp/exampleLib.idl").getCanonicalPath();
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String genDir = "../core/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/exampleLib/src/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath);
        IDLDefaultCodeParser idlParser = new IDLDefaultCodeParser(basePackage, "IDL-Test", idlReader, cppSourceDir);
        idlParser.generateClass = true;
        JParser.generate(idlParser, baseJavaDir, genDir);
    }

    private static void generateAndBuild() throws Exception {
        String libName = "exampleLib";
        String basePackage = "com.github.xpenatan.jparser.example.lib";
        String idlPath = new File("src/main/cpp/exampleLib.idl").getCanonicalPath();
        String baseJavaDir = new File(".").getAbsolutePath() + "./base/src/main/java";
        String cppSourceDir = new File("./src/main/cpp/source/exampleLib/src/").getCanonicalPath();
        String customSourceDir = new File("./src/main/cpp/custom/").getCanonicalPath();

        IDLReader idlReader = IDLReader.readIDL(idlPath);

        String libsDir = new File("./build/c++/libs/").getCanonicalPath();
        String genDir = "../core/src/main/java";
        String libBuildPath = new File("./build/c++/").getCanonicalPath();
        String cppDestinationPath = libBuildPath + "/src";
        String libDestinationPath = cppDestinationPath + "/exampleLib";

        // Move original source code to destination build directory
        FileHelper.copyDir(cppSourceDir, libDestinationPath);

        // Move custom code to destination build directory
        FileHelper.copyDir(customSourceDir, libDestinationPath);

        CppGenerator cppGenerator = new NativeCPPGenerator(libDestinationPath);
        CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, basePackage, customSourceDir);
        cppParser.generateClass = true;
        JParser.generate(cppParser, baseJavaDir, genDir);

        BuildConfig buildConfig = new BuildConfig(
                cppDestinationPath,
                libBuildPath,
                libsDir,
                libName
        );

        String teaVMgenDir = "../teavm/src/main/java/";
        TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, libName, basePackage, customSourceDir);
        JParser.generate(teavmParser, baseJavaDir, teaVMgenDir);

//        IOSTarget iosTarget = new IOSTarget();
//        iosTarget.headerDirs.add("-Isrc/exampleLib");
//        iosTarget.cppIncludes.add("**/src/exampleLib/**.cpp");

        JBuilder.build(buildConfig, getWindowTarget(), getEmscriptenTarget(idlReader), getAndroidTarget());
//        JBuilder.build(buildConfig, getEmscriptenTarget(idlReader));
    }

    private static BuildMultiTarget getWindowTarget() {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        WindowsTarget windowsTarget = new WindowsTarget();
        windowsTarget.isStatic = true;
        windowsTarget.addJNI = false;
        windowsTarget.headerDirs.add("-Isrc/exampleLib");
        windowsTarget.cppIncludes.add("**/src/exampleLib/**.cpp");
        multiTarget.add(windowsTarget);

        WindowsTarget glueTarget = new WindowsTarget();
        glueTarget.linkerFlags.add("../../libs/windows/exampleLib64.a");
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        multiTarget.add(glueTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getEmscriptenTarget(IDLReader idlReader) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        EmscriptenLibTarget emscriptenTarget = new EmscriptenLibTarget();
        emscriptenTarget.libName = "exampleLibside";
        emscriptenTarget.headerDirs.add("-Isrc/exampleLib");
        emscriptenTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
        emscriptenTarget.cppIncludes.add("**/src/exampleLib/**.cpp");
        emscriptenTarget.cppFlags.add("-fPIC");
        emscriptenTarget.cppFlags.add("-sEXPORT_ALL=1");
        emscriptenTarget.linkerFlags.add("-v");
        emscriptenTarget.linkerFlags.add("-fPIC");
        emscriptenTarget.linkerFlags.add("-sSIDE_MODULE=1");
        emscriptenTarget.linkerFlags.add("-sEXPORT_ALL=1");
        emscriptenTarget.libSuffix = ".wasm";
        multiTarget.add(emscriptenTarget);

        EmscriptenTarget mainTarget = new EmscriptenTarget(idlReader);
        mainTarget.headerDirs.add("-Isrc/exampleLib");
        mainTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
        mainTarget.cppFlags.add("-fPIC");
        mainTarget.linkerFlags.add("-sMAIN_MODULE=2");
        mainTarget.linkerFlags.add("-fPIC");
        mainTarget.linkerFlags.add("-ERROR_ON_UNDEFINED_SYMBOLS=0");
        mainTarget.linkerFlags.add("../../libs/emscripten/exampleLibside.wasm");
        multiTarget.add(mainTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget() {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        AndroidTarget androidTarget = new AndroidTarget();
        androidTarget.headerDirs.add("src/exampleLib");
        androidTarget.cppIncludes.add("**/src/exampleLib/**.cpp");

        multiTarget.add(androidTarget);

        return multiTarget;
    }
}
