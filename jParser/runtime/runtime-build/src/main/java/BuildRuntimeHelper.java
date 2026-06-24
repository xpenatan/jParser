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
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class BuildRuntimeHelper {

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
        data.moduleJNISuffix = "-jvm/jni";
        data.moduleWebSuffix = "-jvm/web";
        data.moduleFFMSuffix = "-jvm/ffm";
        BuildToolOptions op = new BuildToolOptions(data, args);
        // Runtime helper methods are predominantly simple set/get operations.
        // Keep critical mode enabled by default for the generated runtime helper.
        op.ffmClassData.defaultCritical = true;
        op.ffmClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        op.teaVMCClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        op.jniClassData.symbolNameMode = JNIClassData.SymbolNameMode.OBFUSCATED;

        op.ffmClassData.logMethod = true;
        FFMNativeBuildConfig ffmNativeBuildConfig = createFFMNativeBuildConfig();

        BuilderTool.build(op, new BuildToolListener() {
            @Override
            public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                if(op.containsArg("web_wasm")) {
                    targets.add(getTeavmTarget(op, idlReader));
                }
                if(op.containsArg("windows64_jni")) {
                    targets.add(getWindowVCTarget(op, "jni", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("linux64_jni")) {
                    targets.add(getLinuxTarget(op, "jni", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("mac64_jni")) {
                    targets.add(getMacTarget(op, false, "jni", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("macArm_jni")) {
                    targets.add(getMacTarget(op, true, "jni", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("android_jni")) {
                    targets.add(getAndroidTarget(op, "jni"));
                }
                if(op.containsArg("ios_jni")) {
                    targets.add(getIOSTarget(op, "jni"));
                }

                if(op.containsArg("windows64_ffm")) {
                    targets.add(getWindowVCTarget(op, "ffm", true, ffmNativeBuildConfig));
                }
                if(op.containsArg("linux64_ffm")) {
                    targets.add(getLinuxTarget(op, "ffm", true, ffmNativeBuildConfig));
                }
                if(op.containsArg("mac64_ffm")) {
                    targets.add(getMacTarget(op, false, "ffm", true, ffmNativeBuildConfig));
                }
                if(op.containsArg("macArm_ffm")) {
                    targets.add(getMacTarget(op, true, "ffm", true, ffmNativeBuildConfig));
                }

                if(op.containsArg("windows64_teavm_c")) {
                    targets.add(getWindowVCTarget(op, "teavm_c", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("linux64_teavm_c")) {
                    targets.add(getLinuxTarget(op, "teavm_c", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("mac64_teavm_c")) {
                    targets.add(getMacTarget(op, false, "teavm_c", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("macArm_teavm_c")) {
                    targets.add(getMacTarget(op, true, "teavm_c", false, ffmNativeBuildConfig));
                }
                if(op.containsArg("android_teavm_c")) {
                    targets.add(getAndroidTarget(op, "teavm_c"));
                }
                if(op.containsArg("ios_teavm_c")) {
                    targets.add(getIOSTarget(op, "teavm_c"));
                }
            }
        });
    }


    private static BuildMultiTarget getWindowVCTarget(BuildToolOptions op, String api, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std:c++17");
        applyFFMWindowsCompileFlags(compileStaticTarget, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        linkTarget.cppFlags.add("-std:c++17");
        applyFFMWindowsCompileFlags(linkTarget, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libBuildCPPPath + "/libs/windows/vc/" + api + "/" + op.libName + "64_.lib");
        linkTarget.linkerFlags.add("-DLL");
        applyFFMWindowsLinkFlags(linkTarget, isFFM, ffmNativeBuildConfig);
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget(BuildToolOptions op, String api, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        LinuxTarget compileStaticTarget = new LinuxTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/"+ api + "/lib" + op.libName + "64_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM, ffmNativeBuildConfig);

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm, String api, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());

        linkTarget.linkerFlags.add("-Wl,-force_load");
        if(isArm) {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/arm/"+ api + "/lib" + op.libName + "64_.a");
        }
        else {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/" + api + "/lib" + op.libName + "64_.a");
        }

        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM, ffmNativeBuildConfig);

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

    private static BuildMultiTarget getAndroidTarget(BuildToolOptions op, String api) {
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
            if(api.equals("teavm_c")) {
                compileStaticTarget.libDirSuffix += api;
            }
            compileStaticTarget.isStatic = true;
            compileStaticTarget.cppFlags.add("-std=c++17");
            compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
            compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, apiLevel);
            if(api.equals("teavm_c")) {
                linkTarget.libDirSuffix += api;
            }
            setupGlueCode(linkTarget, api, libBuildCPPPath);
            linkTarget.cppFlags.add("-std=c++17");
            linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            linkTarget.linkerFlags.add("-Wl,--whole-archive");
            String staticLibPath = libBuildCPPPath + "/libs/android/" + target.getFolder() + "/";
            if(api.equals("teavm_c")) {
                staticLibPath += api + "/";
            }
            linkTarget.linkerFlags.add(staticLibPath + "lib" + op.libName + ".a");
            linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
            linkTarget.linkerFlags.add("-Wl,-z,max-page-size=16384");
            multiTarget.add(linkTarget);
        }

        return multiTarget;
    }

    private static BuildMultiTarget getIOSTarget(BuildToolOptions op, String api)  {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // TODO WIP/not working

        // Make a static library
        IOSTarget compileStaticTarget = new IOSTarget();
        if(api.equals("teavm_c")) {
            compileStaticTarget.libDirSuffix += api;
        }
        compileStaticTarget.isStatic = true;
        compileStaticTarget.cppFlags.add("-std=c++17");
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        if(api.equals("teavm_c")) {
            linkTarget.libDirSuffix += api;
        }
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        linkTarget.cppFlags.add("-std=c++17");
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,-force_load");
        String staticLibPath = libBuildCPPPath + "/libs/ios/";
        if(api.equals("teavm_c")) {
            staticLibPath += api + "/";
        }
        linkTarget.linkerFlags.add(staticLibPath + "lib" + op.libName + "_.a");
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static void setupGlueCode(com.github.xpenatan.jParser.builder.DefaultBuildTarget target, String api, String libBuildCPPPath) {
        if(api.equals("ffm")) {
            target.setupFFMGlueCode(libBuildCPPPath);
        }
        else if(api.equals("teavm_c")) {
            target.setupTeaVMCGlueCode(libBuildCPPPath);
        }
        else {
            target.setupJNIGlueCode(libBuildCPPPath);
        }
    }

    private static void applyFFMWindowsCompileFlags(WindowsMSVCTarget target, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.optimize) {
            addFlagIfMissing(target.cppFlags, "/O2");
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(target.cppFlags, "/GL");
        }
        if(ffmNativeBuildConfig.pgoGenerate || ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(target.cppFlags, "/GL");
        }
    }

    private static void applyFFMWindowsLinkFlags(WindowsMSVCTarget target, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(target.linkerFlags, "/LTCG");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGINSTRUMENT");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGOPTIMIZE");
        }
    }

    private static void applyFFMUnixCompileFlags(ArrayList<String> flags, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.optimize) {
            addFlagIfMissing(flags, "-O3");
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(flags, "-flto");
        }
        if(ffmNativeBuildConfig.hiddenVisibility) {
            addFlagIfMissing(flags, "-fvisibility=hidden");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private static void applyFFMUnixLinkFlags(ArrayList<String> flags, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(flags, "-flto");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private static FFMNativeBuildConfig createFFMNativeBuildConfig() {
        FFMNativeBuildConfig ffmNativeBuildConfig = new FFMNativeBuildConfig();
        // Configure FFM native compiler/linker flags here instead of JVM -D properties.
        ffmNativeBuildConfig.optimize = false;
        ffmNativeBuildConfig.lto = false;
        ffmNativeBuildConfig.hiddenVisibility = false;
        ffmNativeBuildConfig.pgoGenerate = false;
        ffmNativeBuildConfig.pgoUse = false;
        return ffmNativeBuildConfig;
    }

    private static void addFlagIfMissing(ArrayList<String> flags, String flag) {
        if(!flags.contains(flag)) {
            flags.add(flag);
        }
    }

    private static final class FFMNativeBuildConfig {
        private boolean optimize;
        private boolean lto;
        private boolean hiddenVisibility;
        private boolean pgoGenerate;
        private boolean pgoUse;
    }
}
