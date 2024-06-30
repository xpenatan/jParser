import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.builder.targets.AndroidTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenLibTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jparser.builder.targets.IOSTarget;
import com.github.xpenatan.jparser.builder.targets.LinuxTarget;
import com.github.xpenatan.jparser.builder.targets.MacTarget;
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
import java.util.ArrayList;

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

//        NativeCPPGenerator.SKIP_GLUE_CODE = true;
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

//        EmscriptenTarget.SKIP_GLUE_CODE = true;
        TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, libName, basePackage, customSourceDir);
        JParser.generate(teavmParser, baseJavaDir, teaVMgenDir);

        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        if(BuildTarget.isWindows() || BuildTarget.isUnix()) {
            targets.add(getWindowTarget());
            targets.add(getEmscriptenTarget(idlReader));
            targets.add(getAndroidTarget());
        }
        if(BuildTarget.isUnix()) {
            targets.add(getLinuxTarget());
        }
        if(BuildTarget.isMac()) {
            targets.add(getMacTarget());
//            targets.add(getIOSTarget());
        }
        JBuilder.build(buildConfig, targets);
    }

    private static BuildMultiTarget getWindowTarget() throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildPath = new File("./build/c++/").getCanonicalPath().replace("\\", "/");

        // Make a static library
        WindowsTarget windowsTarget = new WindowsTarget();
        windowsTarget.isStatic = true;
        windowsTarget.headerDirs.add("-Isrc/exampleLib");
        windowsTarget.cppInclude.add("**/src/exampleLib/**.cpp");
        multiTarget.add(windowsTarget);

        WindowsTarget glueTarget = new WindowsTarget();
        glueTarget.addJNIHeaders();
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        glueTarget.headerDirs.add("-I" + libBuildPath + "/src/jniglue");
        glueTarget.linkerFlags.add("../../libs/windows/exampleLib64.a");
        glueTarget.cppInclude.add(libBuildPath + "/src/jniglue/JNIGlue.cpp");

//        BuildExtCode.build();
//        String libExtPath = new File("../../lib-ext/").getCanonicalPath().replace("\\", "/");
//        String libExtCPPPath = libExtPath + "/ext-generator/build/c++";
//        glueTarget.headerDirs.add("-I" + libExtCPPPath + "/src/extlib");
//        glueTarget.headerDirs.add("-I" + libExtCPPPath + "/src/jniglue");
//        glueTarget.linkerFlags.add(libExtCPPPath + "/libs/windows/extlib64.a");
//        glueTarget.headerDirs.add("-include" + libExtCPPPath + "/src/jniglue/JNIGlue.h");

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
        androidTarget.addJNIHeaders();
        androidTarget.headerDirs.add("src/exampleLib");
        androidTarget.cppInclude.add("**/src/exampleLib/**.cpp");

        multiTarget.add(androidTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget() throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildPath = new File("./build/c++/").getCanonicalPath().replace("\\", "/");

        // Make a static library
        LinuxTarget linuxTarget = new LinuxTarget();
        linuxTarget.isStatic = true;
        linuxTarget.headerDirs.add("-Isrc/exampleLib");
        linuxTarget.cppInclude.add("**/src/exampleLib/**.cpp");
        multiTarget.add(linuxTarget);

        LinuxTarget glueTarget = new LinuxTarget();
        glueTarget.addJNIHeaders();
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        glueTarget.headerDirs.add("-I" + libBuildPath + "/src/jniglue");
        glueTarget.linkerFlags.add("../../libs/linux/libexampleLib64.a");
        glueTarget.cppInclude.add(libBuildPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(glueTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget() throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildPath = new File("./build/c++/").getCanonicalPath().replace("\\", "/");

        // Make a static library
        MacTarget macTarget = new MacTarget();
        macTarget.isStatic = true;
        macTarget.headerDirs.add("-Isrc/exampleLib");
        macTarget.cppInclude.add("**/src/exampleLib/**.cpp");
        multiTarget.add(macTarget);

        MacTarget glueTarget = new MacTarget();
        glueTarget.addJNIHeaders();
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        glueTarget.headerDirs.add("-I" + libBuildPath + "/src/jniglue");
        glueTarget.linkerFlags.add("../../libs/mac/libexampleLib64.a");
        glueTarget.cppInclude.add(libBuildPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(glueTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getIOSTarget() throws IOException {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildPath = new File("./build/c++/").getCanonicalPath().replace("\\", "/");

        // Make a static library
        IOSTarget iosTarget = new IOSTarget();
        iosTarget.isStatic = true;
        iosTarget.headerDirs.add("-Isrc/exampleLib");
        iosTarget.cppInclude.add("**/src/exampleLib/**.cpp");
        multiTarget.add(iosTarget);

        IOSTarget glueTarget = new IOSTarget();
        glueTarget.addJNIHeaders();
        glueTarget.headerDirs.add("-Isrc/exampleLib");
        glueTarget.headerDirs.add("-I" + libBuildPath + "/src/jniglue");
        glueTarget.linkerFlags.add("../../libs/ios/exampleLib64.a");
        glueTarget.cppInclude.add(libBuildPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(glueTarget);

        return multiTarget;
    }

}
