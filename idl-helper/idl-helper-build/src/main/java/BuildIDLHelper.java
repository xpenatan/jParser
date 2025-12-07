import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import com.github.xpenatan.jParser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jParser.builder.targets.IOSTarget;
import com.github.xpenatan.jParser.builder.targets.LinuxTarget;
import com.github.xpenatan.jParser.builder.targets.MacTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.builder.tool.BuildToolListener;
import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.builder.tool.BuilderTool;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class BuildIDLHelper {

    public static void main(String[] args) throws Exception {
        String libName = "idl";
        String modulePrefix = "idl-helper";
        String basePackage = "com.github.xpenatan.jparser.idl";

        WindowsMSVCTarget.DEBUG_BUILD = false;
//        JParser.CREATE_IDL_HELPER = false;
//        NativeCPPGenerator.SKIP_GLUE_CODE = true;

        BuildToolOptions.BuildToolParams data = new BuildToolOptions.BuildToolParams();
        data.libName = libName;
        data.idlName = null;
        data.webModuleName = libName;
        data.packageName = basePackage;
        data.cppSourcePath = null;
        data.modulePrefix = modulePrefix;
        BuildToolOptions op = new BuildToolOptions(data, args);

        BuilderTool.build(op, new BuildToolListener() {
            @Override
            public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                if(op.containsArg("teavm")) {
                    targets.add(getTeavmTarget(op, idlReader));
                }
                if(op.containsArg("windows64")) {
                    targets.add(getWindowVCTarget(op));
                }
                if(op.containsArg("linux64")) {
                    targets.add(getLinuxTarget(op));
                }
                if(op.containsArg("mac64")) {
                    targets.add(getMacTarget(op, false));
                }
                if(op.containsArg("macArm")) {
                    targets.add(getMacTarget(op, true));
                }
                if(op.containsArg("android")) {
                    targets.add(getAndroidTarget(op));
                }
                if(op.containsArg("ios")) {
                    targets.add(getIOSTarget(op));
                }
            }
        });
    }

    private static BuildMultiTarget getWindowVCTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std:c++11");
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std:c++11");
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libBuildCPPPath + "/libs/windows/vc/" + op.libName + "64_.lib");
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
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/lib" + op.libName + "64_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");

        linkTarget.linkerFlags.add("-Wl,-force_load");
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

        // Make a static library
        EmscriptenTarget compileStaticTarget = new EmscriptenTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.compileGlueCode = false;
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/idl/");
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.cppFlags.add("-fPIC");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        // Compile glue code and link to make js file
        EmscriptenTarget linkTarget = new EmscriptenTarget();
        linkTarget.idlReader = idlReader;
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/idl/");
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.cppFlags.add("-fPIC");
        linkTarget.headerDirs.add("-include" + op.getCustomSourceDir() + "IDLCustomCode.h");
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/emscripten/" + op.libName + "_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
//        linkTarget.linkerFlags.add("-Wl,--export-all");
        linkTarget.linkerFlags.add("--use-port=emdawnwebgpu");
        linkTarget.exportedRuntimeMethods.add("WebGPU");
        linkTarget.linkerFlags.add("-sMAIN_MODULE=1");
        linkTarget.exportedFunctions.add("_free");
        linkTarget.exportedFunctions.add("_malloc");
        linkTarget.exportedFunctions.add("___stack_low");
        linkTarget.exportedFunctions.add("___stack_high");
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget(BuildToolOptions op) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        AndroidTarget.ApiLevel apiLevel = AndroidTarget.ApiLevel.Android_10_29;
        ArrayList<AndroidTarget.Target> targets = new ArrayList<>();

        targets.add(AndroidTarget.Target.x86);
        targets.add(AndroidTarget.Target.x86_64);
        targets.add(AndroidTarget.Target.armeabi_v7a);
        targets.add(AndroidTarget.Target.arm64_v8a);

        for(int i = 0; i < targets.size(); i++) {
            AndroidTarget.Target target = targets.get(i);

            // Make a static library
            AndroidTarget compileStaticTarget = new AndroidTarget(target, apiLevel);
            compileStaticTarget.isStatic = true;
            compileStaticTarget.cppFlags.add("-std=c++11");
            compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
            compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, apiLevel);
            linkTarget.addJNIHeaders();
            linkTarget.cppFlags.add("-std=c++11");
            linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
            linkTarget.linkerFlags.add("-Wl,--whole-archive");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/android/" + target.getFolder() +"/lib" + op.libName + ".a");
            linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
            linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");
            linkTarget.linkerFlags.add("-Wl,-z,max-page-size=16384");
            multiTarget.add(linkTarget);
        }

        return multiTarget;
    }

    private static BuildMultiTarget getIOSTarget(BuildToolOptions op)  {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // TODO WIP/not working

        // Make a static library
        IOSTarget compileStaticTarget = new IOSTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/idl/IDLHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.linkerFlags.add("-Wl,-force_load");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/ios/lib" + op.libName + "_.a");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");
        multiTarget.add(linkTarget);

        return multiTarget;
    }
}