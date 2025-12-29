import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import com.github.xpenatan.jParser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jParser.builder.targets.IOSTarget;
import com.github.xpenatan.jParser.builder.targets.LinuxTarget;
import com.github.xpenatan.jParser.builder.targets.MacTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsTarget;
import com.github.xpenatan.jParser.builder.tool.BuildToolListener;
import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.builder.tool.BuilderTool;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.File;
import java.util.ArrayList;

public class BuildLibB {

    public static void main(String[] args) throws Exception {
        String libName = "LibB";
        String modulePrefix = "lib";
        String basePackage = "libB";
        String sourceDir = "/src/main/cpp/source";

        WindowsMSVCTarget.DEBUG_BUILD = false;
        JParser.CREATE_IDL_HELPER = false;
//        NativeCPPGenerator.SKIP_GLUE_CODE = true;

        String libAPath = new File("./../../libA/").getCanonicalPath().replace("\\", "/");

        BuildToolOptions.BuildToolParams data = new BuildToolOptions.BuildToolParams();
        data.libName = libName;
        data.idlName = libName;
        data.webModuleName = libName;
        data.packageName = basePackage;
        data.cppSourcePath = sourceDir;
        data.modulePrefix = modulePrefix;

        BuildToolOptions op = new BuildToolOptions(data, args);

        op.addAdditionalIDLRefPath(IDLReader.parseFile(libAPath + "/lib-build/src/main/cpp/LibA.idl"));
        op.addAdditionalIDLRefPath(IDLReader.getIDLHelperFile());

        BuilderTool.build(op, new BuildToolListener() {
            @Override
            public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                if(op.containsArg("teavm")) {
                    targets.add(getTeavmTarget(op, idlReader, libAPath));
                }
                if(op.containsArg("windows64")) {
                    targets.add(getWindowVCTarget(op, libAPath));
//                    targets.add(getWindowTarget(op, libAPath));
                }
                if(op.containsArg("linux64")) {
                    targets.add(getLinuxTarget(op, libAPath));
                }
                if(op.containsArg("mac64")) {
                    targets.add(getMacTarget(op, false, libAPath));
                }
                if(op.containsArg("macArm")) {
                    targets.add(getMacTarget(op, true, libAPath));
                }
                if(op.containsArg("android")) {
                    targets.add(getAndroidTarget(op, libAPath));
                }
                if(op.containsArg("ios")) {
                    targets.add(getIOSTarget(op, libAPath));
                }
            }
        });
    }

    private static BuildMultiTarget getWindowTarget(BuildToolOptions op, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/libs/windows";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsTarget compileStaticTarget = new WindowsTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsTarget linkTarget = new WindowsTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/windows/" + op.libName + "64_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getWindowVCTarget(BuildToolOptions op, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/c++/libs/windows/vc";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String config = "/DLIB_USER_CONFIG=\"\\\"LibACustomConfig.h\\\"\"";

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("/std:c++11");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + libASourcePath);
        compileStaticTarget.headerDirs.add("-I" + libACustomPath);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("/std:c++11");
        linkTarget.cppFlags.add(config);
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.headerDirs.add("-I" + libASourcePath);
        linkTarget.headerDirs.add("-I" + libACustomPath);
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libALibPath + "/LibA64.lib");
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libBuildCPPPath + "/libs/windows/vc/" + op.libName + "64_.lib");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget(BuildToolOptions op, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/c++/libs/linux";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String config = "-DLIB_USER_CONFIG=\"LibACustomConfig.h\"";

        // Make a static library
        LinuxTarget compileStaticTarget = new LinuxTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.cppFlags.add(config);
        compileStaticTarget.cppFlags.add("-fPIC");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + libASourcePath);
        compileStaticTarget.headerDirs.add("-I" + libACustomPath);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.cppFlags.add(config);
        linkTarget.cppFlags.add("-fPIC");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.headerDirs.add("-I" + libASourcePath);
        linkTarget.headerDirs.add("-I" + libACustomPath);
        linkTarget.linkerFlags.add(libALibPath + "/libLibA64.so");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/lib" + op.libName + "64_.a");
        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/c++/libs/mac";
        String libALibArmPath = libALibPath + "/arm";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String config = "-DLIB_USER_CONFIG=\"LibACustomConfig.h\"";

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.cppFlags.add(config);
        compileStaticTarget.cppFlags.add("-fPIC");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + libASourcePath);
        compileStaticTarget.headerDirs.add("-I" + libACustomPath);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.addJNIHeaders();
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.cppFlags.add(config);
        linkTarget.cppFlags.add("-fPIC");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
        linkTarget.headerDirs.add("-I" + libASourcePath);

        if(isArm) {
            linkTarget.linkerFlags.add(libALibArmPath + "/LibA64_.a");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/arm/lib" + op.libName + "64_.a");
        }
        else {
            linkTarget.linkerFlags.add(libALibPath + "/LibA64_.a");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/lib" + op.libName + "64_.a");
        }

        linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getTeavmTarget(BuildToolOptions op, IDLReader idlReader, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/c++/libs/emscripten";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        EmscriptenTarget compileStaticTarget = new EmscriptenTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.compileGlueCode = false;
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + libASourcePath);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppFlags.add("-std=c++11");
        multiTarget.add(compileStaticTarget);

        // Compile glue code and link to make js file
        EmscriptenTarget linkTarget = new EmscriptenTarget();
        linkTarget.idlReader = idlReader;
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + libASourcePath);
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.headerDirs.add("-include" + op.getCustomSourceDir() + "LibBCustomCode.h");
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/emscripten/" + op.libName + "_.a");
//        linkTarget.linkerFlags.add(libALibPath + "/LibA_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        linkTarget.mainModuleName = "idl";
        linkTarget.linkerFlags.add("-sSIDE_MODULE=2");
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget(BuildToolOptions op, String libAPath) {
        String libALibPath = libAPath + "/lib-build/build/c++/libs/android/";
        String libACPPPath = libAPath + "/lib-build/src/main/cpp";
        String libASourcePath = libACPPPath + "/source";
        String libACustomPath = libACPPPath + "/custom";

        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        AndroidTarget.ApiLevel apiLevel = AndroidTarget.ApiLevel.Android_10_29;
        ArrayList<AndroidTarget.Target> targets = new ArrayList<>();

        targets.add(AndroidTarget.Target.x86);
        targets.add(AndroidTarget.Target.x86_64);
        targets.add(AndroidTarget.Target.armeabi_v7a);
        targets.add(AndroidTarget.Target.arm64_v8a);

        String config = "-DLIB_USER_CONFIG=\"LibACustomConfig.h\"";

        for(int i = 0; i < targets.size(); i++) {
            AndroidTarget.Target target = targets.get(i);

            // Make a static library
            AndroidTarget compileStaticTarget = new AndroidTarget(target, apiLevel);
            compileStaticTarget.isStatic = true;
            compileStaticTarget.cppFlags.add("-std=c++11");
            compileStaticTarget.cppFlags.add(config);
            compileStaticTarget.headerDirs.add("-I" + sourceDir);
            compileStaticTarget.headerDirs.add("-I" + libASourcePath);
            compileStaticTarget.headerDirs.add("-I" + libACustomPath);
            compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, apiLevel);
            linkTarget.addJNIHeaders();
            linkTarget.cppFlags.add("-std=c++11");
            linkTarget.headerDirs.add("-I" + sourceDir);
            linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/jniglue");
            linkTarget.headerDirs.add("-I" + libASourcePath);
            linkTarget.linkerFlags.add("-Wl,--whole-archive");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/android/" + target.getFolder() +"/lib" + op.libName + ".a");
//            linkTarget.linkerFlags.add(libALibPath + target.getFolder() + "/libLibA.a");
            linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
            linkTarget.linkerFlags.add("-Wl,--allow-shlib-undefined");
            linkTarget.cppInclude.add(libBuildCPPPath + "/src/jniglue/JNIGlue.cpp");
            multiTarget.add(linkTarget);
        }

        return multiTarget;
    }

    private static BuildMultiTarget getIOSTarget(BuildToolOptions op, String libAPath)  {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // TODO WIP/not working

        // Make a static library
        IOSTarget compileStaticTarget = new IOSTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
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
        linkTarget.linkerFlags.add("-Wl,-z,max-page-size=16384");
        linkTarget.linkerFlags.add("-Wl,-undefined,dynamic_lookup");
        multiTarget.add(linkTarget);

        return multiTarget;
    }
}
