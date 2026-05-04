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
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class BuildRuntimeHelper {

    private static final boolean FFM_NATIVE_OPTIMIZE = Boolean.getBoolean("jparser.ffm.nativeOptimize");
    private static final boolean FFM_NATIVE_LTO = Boolean.getBoolean("jparser.ffm.nativeLto");
    private static final boolean FFM_NATIVE_HIDDEN_VISIBILITY = Boolean.getBoolean("jparser.ffm.nativeHiddenVisibility");
    private static final boolean FFM_NATIVE_PGO_GENERATE = Boolean.getBoolean("jparser.ffm.nativePgoGenerate");
    private static final boolean FFM_NATIVE_PGO_USE = Boolean.getBoolean("jparser.ffm.nativePgoUse");

    public static void main(String[] args) throws Exception {
        String libName = "runtime";
        String modulePrefix = "runtime";
        String basePackage = "com.github.xpenatan.jparser.runtime";

        WindowsMSVCTarget.DEBUG_BUILD = false;
        JParser.CREATE_RUNTIME_HELPER = true;
//        NativeCPPGenerator.SKIP_GLUE_CODE = true;

        BuildToolOptions.BuildToolParams data = new BuildToolOptions.BuildToolParams();
        data.libName = libName;
        data.idlName = null;
        data.webModuleName = libName;
        data.packageName = basePackage;
        data.cppSourcePath = null;
        data.modulePrefix = modulePrefix;
        BuildToolOptions op = new BuildToolOptions(data, args);
        // Runtime helper methods are predominantly simple set/get operations.
        // Keep critical mode enabled by default, while FFMCodeParser still enforces type eligibility safety.
        op.ffmClassData = new FFMClassData(true);

        BuilderTool.build(op, new BuildToolListener() {
            @Override
            public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                if(op.containsArg("web_wasm")) {
                    targets.add(getTeavmTarget(op, idlReader));
                }
                if(op.containsArg("windows64_jni")) {
                    targets.add(getWindowVCTarget(op, false));
                }
                if(op.containsArg("linux64_jni")) {
                    targets.add(getLinuxTarget(op, false));
                }
                if(op.containsArg("mac64_jni")) {
                    targets.add(getMacTarget(op, false, false));
                }
                if(op.containsArg("macArm_jni")) {
                    targets.add(getMacTarget(op, true, false));
                }
                if(op.containsArg("android_jni")) {
                    targets.add(getAndroidTarget(op));
                }
                if(op.containsArg("ios_jni")) {
                    targets.add(getIOSTarget(op));
                }

                if(op.containsArg("windows64_ffm")) {
                    targets.add(getWindowVCTarget(op, true));
                }
                if(op.containsArg("linux64_ffm")) {
                    targets.add(getLinuxTarget(op, true));
                }
                if(op.containsArg("mac64_ffm")) {
                    targets.add(getMacTarget(op, false, true));
                }
                if(op.containsArg("macArm_ffm")) {
                    targets.add(getMacTarget(op, true, true));
                }
            }
        });
    }


    private static BuildMultiTarget getWindowVCTarget(BuildToolOptions op, boolean isFFM) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String api = isFFM ? "ffm" : "jni";

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std:c++17");
        applyFFMWindowsCompileFlags(compileStaticTarget, isFFM);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.libDirSuffix += api;
        if(isFFM) {
            linkTarget.setupFFMGlueCode(libBuildCPPPath);
        }
        else {
            linkTarget.setupJNIGlueCode(libBuildCPPPath);
        }
        linkTarget.cppFlags.add("-std:c++17");
        applyFFMWindowsCompileFlags(linkTarget, isFFM);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libBuildCPPPath + "/libs/windows/vc/" + api + "/" + op.libName + "64_.lib");
        linkTarget.linkerFlags.add("-DLL");
        applyFFMWindowsLinkFlags(linkTarget, isFFM);
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget(BuildToolOptions op, boolean isFFM) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String api = isFFM ? "ffm" : "jni";

        // Make a static library
        LinuxTarget compileStaticTarget = new LinuxTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.libDirSuffix += api;
        if(isFFM) {
            linkTarget.setupFFMGlueCode(libBuildCPPPath);
        }
        else {
            linkTarget.setupJNIGlueCode(libBuildCPPPath);
        }
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/"+ api + "/lib" + op.libName + "64_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM);

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm, boolean isFFM) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        String api = isFFM ? "ffm" : "jni";

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.libDirSuffix += api;
        if(isFFM) {
            linkTarget.setupFFMGlueCode(libBuildCPPPath);
        }
        else {
            linkTarget.setupJNIGlueCode(libBuildCPPPath);
        }
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());

        linkTarget.linkerFlags.add("-Wl,-force_load");
        if(isArm) {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/arm/"+ api + "/lib" + op.libName + "64_.a");
        }
        else {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/" + api + "/lib" + op.libName + "64_.a");
        }

        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM);

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
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.cppFlags.add("-fPIC");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        // Compile glue code and link to make js file
        EmscriptenTarget linkTarget = new EmscriptenTarget();
        linkTarget.idlReader = idlReader;
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.headerDirs.add("-I" + libBuildCPPPath + "/src/idl/");
        linkTarget.cppFlags.add("-std=c++17");
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
        linkTarget.exportedFunctions.add("__ZNSt3__24coutE");
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
            compileStaticTarget.cppFlags.add("-std=c++17");
            compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
            compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, apiLevel);
            linkTarget.setupJNIGlueCode(libBuildCPPPath);
            linkTarget.cppFlags.add("-std=c++17");
            linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            linkTarget.linkerFlags.add("-Wl,--whole-archive");
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/android/" + target.getFolder() +"/lib" + op.libName + ".a");
            linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
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
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        linkTarget.setupJNIGlueCode(libBuildCPPPath);
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,-force_load");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/ios/lib" + op.libName + "_.a");
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static void applyFFMWindowsCompileFlags(WindowsMSVCTarget target, boolean isFFM) {
        if(!isFFM) {
            return;
        }
        if(FFM_NATIVE_OPTIMIZE) {
            addFlagIfMissing(target.cppFlags, "/O2");
        }
        if(FFM_NATIVE_LTO) {
            addFlagIfMissing(target.cppFlags, "/GL");
        }
        if(FFM_NATIVE_PGO_GENERATE || FFM_NATIVE_PGO_USE) {
            addFlagIfMissing(target.cppFlags, "/GL");
        }
    }

    private static void applyFFMWindowsLinkFlags(WindowsMSVCTarget target, boolean isFFM) {
        if(!isFFM) {
            return;
        }
        if(FFM_NATIVE_LTO) {
            addFlagIfMissing(target.linkerFlags, "/LTCG");
        }
        if(FFM_NATIVE_PGO_GENERATE) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGINSTRUMENT");
        }
        else if(FFM_NATIVE_PGO_USE) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGOPTIMIZE");
        }
    }

    private static void applyFFMUnixCompileFlags(ArrayList<String> flags, boolean isFFM) {
        if(!isFFM) {
            return;
        }
        if(FFM_NATIVE_OPTIMIZE) {
            addFlagIfMissing(flags, "-O3");
        }
        if(FFM_NATIVE_LTO) {
            addFlagIfMissing(flags, "-flto");
        }
        if(FFM_NATIVE_HIDDEN_VISIBILITY) {
            addFlagIfMissing(flags, "-fvisibility=hidden");
        }
        if(FFM_NATIVE_PGO_GENERATE) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(FFM_NATIVE_PGO_USE) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private static void applyFFMUnixLinkFlags(ArrayList<String> flags, boolean isFFM) {
        if(!isFFM) {
            return;
        }
        if(FFM_NATIVE_LTO) {
            addFlagIfMissing(flags, "-flto");
        }
        if(FFM_NATIVE_PGO_GENERATE) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(FFM_NATIVE_PGO_USE) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private static void addFlagIfMissing(ArrayList<String> flags, String flag) {
        if(!flags.contains(flag)) {
            flags.add(flag);
        }
    }
}