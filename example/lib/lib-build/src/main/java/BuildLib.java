import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.targets.AndroidTarget;
import com.github.xpenatan.jparser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jparser.builder.targets.IOSTarget;
import com.github.xpenatan.jparser.builder.targets.LinuxTarget;
import com.github.xpenatan.jparser.builder.targets.MacTarget;
import com.github.xpenatan.jparser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jparser.builder.targets.WindowsTarget;
import com.github.xpenatan.jparser.builder.tool.BuildToolListener;
import com.github.xpenatan.jparser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jparser.builder.tool.BuilderTool;
import com.github.xpenatan.jparser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;

public class BuildLib {

    public static void main(String[] args) throws Exception {
        String libName = "TestLib";
        String modulePrefix = "lib";
        String basePackage = "com.github.xpenatan.jparser.example.testlib";
        String sourceDir = "/src/main/cpp/source/TestLib/src";

        WindowsMSVCTarget.DEBUG_BUILD = true;
//        NativeCPPGenerator.SKIP_GLUE_CODE = true;

        BuildToolOptions op = new BuildToolOptions(libName, basePackage, modulePrefix, sourceDir, args);
        BuilderTool.build(op, new BuildToolListener() {
            @Override
            public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                if(op.teavm) {
                    targets.add(getTeavmTarget(op, idlReader));
                }
                if(op.windows64) {
                    targets.add(getWindowVCTarget(op));
                    targets.add(getWindowTarget(op));
                }
                if(op.linux64) {
                    targets.add(getLinuxTarget(op));
                }
                if(op.mac64) {
                    targets.add(getMacTarget(op, false));
                }
                if(op.macArm) {
                    targets.add(getMacTarget(op, true));
                }
                if(op.android) {
                    targets.add(getAndroidTarget(op));
                }
                if(op.iOS) {
                    targets.add(getIOSTarget(op));
                }
            }
        });
    }

    private static BuildMultiTarget getWindowTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsTarget compileStaticTarget = new WindowsTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsTarget linkTarget = new WindowsTarget();
        linkTarget.addJNIHeaders();
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/windows/" + op.libName + "64_.a");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private static BuildMultiTarget getWindowVCTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.addJNIHeaders();
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/windows/vc/" + op.libName + "64_.lib");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        LinuxTarget compileStaticTarget = new LinuxTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-Isrc/TestLib");
        compileStaticTarget.cppInclude.add("**/src/TestLib/**.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.addJNIHeaders();
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/lib" + op.libName + "64_.a");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.addJNIHeaders();
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");

        if(isArm) {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/arm/lib" + op.libName + "64_.a");
        }
        else {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/lib" + op.libName + "64_.a");
        }
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getTeavmTarget(BuildToolOptions op, IDLReader idlReader) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        int buildType = 1;

        if(buildType == 0) {
//            // Compile and create a js file
//            EmscriptenTarget emscriptenTarget = new EmscriptenTarget(idlReader);
//            emscriptenTarget.headerDirs.add("-Isrc/TestLib");
//            emscriptenTarget.headerDirs.add("-includesrc/TestLib/CustomCode.h");
//            emscriptenTarget.cppInclude.add("**/src/TestLib/**.cpp");
//            multiTarget.add(emscriptenTarget);
        }
        else if(buildType == 1) {
            // Make a static library
            EmscriptenTarget compileStaticTarget = new EmscriptenTarget(idlReader);
            compileStaticTarget.isStatic = true;
            compileStaticTarget.compileGlueCode = false;
            compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
            compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");
            multiTarget.add(compileStaticTarget);

            // Compile glue code and link to make js file
            EmscriptenTarget linkTarget = new EmscriptenTarget(idlReader);
            linkTarget.headerDirs.add("-include" + libBuildCPPPath + "/src/TestLib/CustomCode.h");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/emscripten/" + op.libName + "_.a");
            multiTarget.add(linkTarget);
        }
        else if(buildType == 2) {
//            // Make lib as a side module/dynamic linking
//            EmscriptenLibTarget sideTarget = new EmscriptenLibTarget();
//            sideTarget.libName = "TestLibside";
//            sideTarget.headerDirs.add("-Isrc/TestLib");
//            sideTarget.headerDirs.add("-includesrc/TestLib/CustomCode.h");
//            sideTarget.cppInclude.add("**/src/TestLib/**.cpp");
//            sideTarget.cppFlags.add("-fPIC");
//            sideTarget.cppFlags.add("-sEXPORT_ALL=1");
//            sideTarget.linkerFlags.add("-v");
//            sideTarget.linkerFlags.add("-fPIC");
//            sideTarget.linkerFlags.add("-sSIDE_MODULE=1");
//            sideTarget.linkerFlags.add("-sEXPORT_ALL=1");
//            sideTarget.libSuffix = ".wasm";
//            multiTarget.add(sideTarget);
//
//            // Make lib as a main module
//            EmscriptenTarget mainTarget = new EmscriptenTarget(idlReader);
//            mainTarget.headerDirs.add("-Isrc/TestLib");
//            mainTarget.headerDirs.add("-includesrc/TestLib/CustomCode.h");
//            mainTarget.cppFlags.add("-fPIC");
//            mainTarget.linkerFlags.add("-sMAIN_MODULE=2");
//            mainTarget.linkerFlags.add("-fPIC");
//            mainTarget.linkerFlags.add("-ERROR_ON_UNDEFINED_SYMBOLS=0");
//            mainTarget.linkerFlags.add("../../libs/emscripten/TestLibside.wasm");
//            multiTarget.add(mainTarget);
        }
        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();

        String libBuildCPPPath = op.getModuleBuildCPPPath();

        AndroidTarget androidTarget = new AndroidTarget();
        androidTarget.addJNIHeaders();
        androidTarget.headerDirs.add(libBuildCPPPath + "/src/TestLib");
        androidTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");

        multiTarget.add(androidTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getIOSTarget(BuildToolOptions op)  {
        String libBuildCPPPath = op.getModuleBuildCPPPath();
        // TODO WIP/not working

        BuildMultiTarget multiTarget = new BuildMultiTarget();

        // Make a static library
        IOSTarget compileStaticTarget = new IOSTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/TestLib/**.cpp");
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        linkTarget.addJNIHeaders();
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/TestLib");
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/ios/lib" + op.libName + "_.a");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }
}