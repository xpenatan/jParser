import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.builder.targets.AndroidTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenLibTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenTarget;
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
import java.io.IOException;

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

//        JBuilder.build(buildConfig, getWindowTarget(), getEmscriptenTarget(idlReader), getAndroidTarget());
//        JBuilder.build(buildConfig, getEmscriptenTarget(idlReader));
        JBuilder.build(buildConfig, getWindowTarget());
    }

    private static BuildMultiTarget getWindowTarget() throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libPath = new File("../../lib-ext/").getCanonicalPath().replace("\\", "/");
        String libCPPPath = libPath + "/ext-generator/build/c++";

//        // Make a static library
//        WindowsTarget windowsTarget = new WindowsTarget();
//        windowsTarget.isStatic = true;
//        windowsTarget.addJNI = false;
//        windowsTarget.headerDirs.add("-Isrc/exampleLib");
//        windowsTarget.cppInclude.add("**/src/exampleLib/**.cpp");
//        multiTarget.add(windowsTarget);

        WindowsTarget glueTarget = new WindowsTarget();
        glueTarget.linkerFlags.add("../../libs/windows/exampleLib64.a");
        glueTarget.linkerFlags.add(libCPPPath + "/libs/windows/extlib64.a");
        glueTarget.headerDirs.add("-include" + libCPPPath + "/src/extlib/CustomLib.h");
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        glueTarget.headerDirs.add("-I" + libCPPPath + "src/extlib");
        multiTarget.add(glueTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getEmscriptenTarget(IDLReader idlReader) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        int buildType = 1;

        if(buildType == 0) {
            // Compile and create a js file
            EmscriptenTarget emscriptenTarget = new EmscriptenTarget(idlReader);
            emscriptenTarget.headerDirs.add("-Isrc/exampleLib");
            emscriptenTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
            emscriptenTarget.cppInclude.add("**/src/exampleLib/**.cpp");
            multiTarget.add(emscriptenTarget);
        }
        else if(buildType == 1) {
            // Make a static library
            EmscriptenTarget libTarget = new EmscriptenTarget(idlReader);
            libTarget.isStatic = true;
            libTarget.compileGlueCode = false;
            libTarget.headerDirs.add("-Isrc/exampleLib");
            libTarget.cppInclude.add("**/src/exampleLib/**.cpp");
            multiTarget.add(libTarget);

            // Compile glue code and link to make js file
            EmscriptenTarget linkTarget = new EmscriptenTarget(idlReader);
            linkTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
            linkTarget.linkerFlags.add("../../libs/emscripten/exampleLib.a");
            multiTarget.add(linkTarget);
        }
        else if(buildType == 2) {
            // Make lib as a side module/dynamic linking
            EmscriptenLibTarget sideTarget = new EmscriptenLibTarget();
            sideTarget.libName = "exampleLibside";
            sideTarget.headerDirs.add("-Isrc/exampleLib");
            sideTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
            sideTarget.cppInclude.add("**/src/exampleLib/**.cpp");
            sideTarget.cppFlags.add("-fPIC");
            sideTarget.cppFlags.add("-sEXPORT_ALL=1");
            sideTarget.linkerFlags.add("-v");
            sideTarget.linkerFlags.add("-fPIC");
            sideTarget.linkerFlags.add("-sSIDE_MODULE=1");
            sideTarget.linkerFlags.add("-sEXPORT_ALL=1");
            sideTarget.libSuffix = ".wasm";
            multiTarget.add(sideTarget);

            // Make lib as a main module
            EmscriptenTarget mainTarget = new EmscriptenTarget(idlReader);
            mainTarget.headerDirs.add("-Isrc/exampleLib");
            mainTarget.headerDirs.add("-includesrc/exampleLib/CustomCode.h");
            mainTarget.cppFlags.add("-fPIC");
            mainTarget.linkerFlags.add("-sMAIN_MODULE=2");
            mainTarget.linkerFlags.add("-fPIC");
            mainTarget.linkerFlags.add("-ERROR_ON_UNDEFINED_SYMBOLS=0");
            mainTarget.linkerFlags.add("../../libs/emscripten/exampleLibside.wasm");
            multiTarget.add(mainTarget);
        }
        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget() {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        AndroidTarget androidTarget = new AndroidTarget();
        androidTarget.headerDirs.add("src/exampleLib");
        androidTarget.cppInclude.add("**/src/exampleLib/**.cpp");

        multiTarget.add(androidTarget);

        return multiTarget;
    }
}
