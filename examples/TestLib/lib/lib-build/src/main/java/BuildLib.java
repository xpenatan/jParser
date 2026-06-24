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
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.ffm.FFMCriticalMethodData;
import com.github.xpenatan.jParser.ffm.FFMCriticalMethodListener;
import com.github.xpenatan.jParser.ffm.FFMCriticalMode;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class BuildLib {

    public static void main(String[] args) throws Exception {
        String libName = "TestLib";
        String modulePrefix = "lib";
        String basePackage = "com.github.xpenatan.jParser.example.testlib";
        String sourceDir = "/src/main/cpp/source/TestLib/src";

        WindowsMSVCTarget.DEBUG_BUILD = true;
//        NativeCPPGenerator.SKIP_GLUE_CODE = true;

        BuildToolOptions.BuildToolParams data = new BuildToolOptions.BuildToolParams();
        data.libName = libName;
        data.idlName = libName;
        data.webModuleName = libName;
        data.packageName = basePackage;
        data.cppSourcePath = sourceDir;
        data.modulePrefix = modulePrefix;

        BuildToolOptions op = new BuildToolOptions(data, args);

        op.ffmClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        op.teaVMCClassData.symbolNameMode = FFMClassData.SymbolNameMode.OBFUSCATED;
        op.jniClassData.symbolNameMode = JNIClassData.SymbolNameMode.OBFUSCATED;

        op.ffmClassData.logMethod = true;
        op.ffmClassData.methodListener = new FFMCriticalMethodListener() {
            @Override
            public FFMCriticalMode onCriticalMode(FFMCriticalMethodData methodData) {
                return null;
            }
        };
        op.addAdditionalIDLRefPath(IDLReader.getRuntimeHelperFile());
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
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        addCppStandard(compileStaticTarget.cppFlags, api, true);
        applyFFMWindowsCompileFlags(compileStaticTarget, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, true);
        applyFFMWindowsCompileFlags(linkTarget, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + libBuildCPPPath + "/libs/windows/vc/" + api + "/" + op.libName + "64_.lib");
        linkTarget.linkerFlags.add("-DLL");
        applyFFMWindowsLinkFlags(linkTarget, isFFM, ffmNativeBuildConfig);
        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getLinuxTarget(BuildToolOptions op, String api, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        LinuxTarget compileStaticTarget = new LinuxTarget();
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        addCppStandard(compileStaticTarget.cppFlags, api, false);
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false);
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/linux/" + api + "/lib" + op.libName + "64_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM, ffmNativeBuildConfig);

        multiTarget.add(linkTarget);

        return multiTarget;
    }

    private static BuildMultiTarget getMacTarget(BuildToolOptions op, boolean isArm, String api, boolean isFFM, FFMNativeBuildConfig ffmNativeBuildConfig) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        MacTarget compileStaticTarget = new MacTarget(isArm);
        compileStaticTarget.libDirSuffix += api;
        addCppStandard(compileStaticTarget.cppFlags, api, false);
        compileStaticTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false);
        linkTarget.cppFlags.add("-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, ffmNativeBuildConfig);
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());

        linkTarget.linkerFlags.add("-Wl,-force_load");
        if(isArm) {
            linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/mac/arm/" + api + "/lib" + op.libName + "64_.a");
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
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        // Make a static library
        EmscriptenTarget compileStaticTarget = new EmscriptenTarget();
        compileStaticTarget.isStatic = true;
        compileStaticTarget.compileGlueCode = false;
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppFlags.add("-std=c++11");
        compileStaticTarget.cppFlags.add("-fPIC");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        // Compile glue code and link to make js file
        EmscriptenTarget linkTarget = new EmscriptenTarget();
        linkTarget.idlReader = idlReader;
        linkTarget.headerDirs.add("-I" + sourceDir);
        linkTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        linkTarget.cppFlags.add("-std=c++11");
        linkTarget.cppFlags.add("-fPIC");
        linkTarget.headerDirs.add("-include" + op.getCustomSourceDir() + "CustomCode.h");
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(libBuildCPPPath + "/libs/emscripten/" + op.libName + "_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        linkTarget.linkerFlags.add("-Wl,--export-all");
        linkTarget.linkerFlags.add("-lc++abi"); // C++ ABI (exceptions, thread_atexit, etc.)
        linkTarget.linkerFlags.add("-lc++"); // C++ STL (std::cout, std::string, etc.)
        linkTarget.linkerFlags.add("-lc"); // C standard library (fopen, fclose, printf, etc.)
        linkTarget.mainModuleName = "runtime";
        linkTarget.linkerFlags.add("-sSIDE_MODULE=2");
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private static BuildMultiTarget getAndroidTarget(BuildToolOptions op, String api) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
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
            addCppStandard(compileStaticTarget.cppFlags, api, false);
            addPICForTeaVMC(compileStaticTarget.cppFlags, api);
            compileStaticTarget.headerDirs.add("-I" + sourceDir);
            compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
            compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
            compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, apiLevel);
            if(api.equals("teavm_c")) {
                linkTarget.libDirSuffix += api;
            }
            setupGlueCode(linkTarget, api, libBuildCPPPath);
            addCppStandard(linkTarget.cppFlags, api, false);
            addPICForTeaVMC(linkTarget.cppFlags, api);
            linkTarget.headerDirs.add("-I" + sourceDir);
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
        addCppStandard(compileStaticTarget.cppFlags, api, false);
        compileStaticTarget.headerDirs.add("-I" + sourceDir);
        compileStaticTarget.headerDirs.add("-I" + op.getCustomSourceDir());
        compileStaticTarget.cppInclude.add(sourceDir + "**.cpp");
        compileStaticTarget.cppInclude.add(op.getCustomSourceDir() + "*.cpp");
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        if(api.equals("teavm_c")) {
            linkTarget.libDirSuffix += api;
        }
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false);
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

    private static void addCppStandard(ArrayList<String> flags, String api, boolean windows) {
        if(windows) {
            flags.add(api.equals("teavm_c") ? "/std:c++17" : "/std:c++11");
        }
        else {
            flags.add(api.equals("teavm_c") ? "-std=c++17" : "-std=c++11");
        }
    }

    private static void addPICForTeaVMC(ArrayList<String> flags, String api) {
        if(api.equals("teavm_c")) {
            addFlagIfMissing(flags, "-fPIC");
        }
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
