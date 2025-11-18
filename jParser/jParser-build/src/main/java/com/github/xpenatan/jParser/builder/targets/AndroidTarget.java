package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class AndroidTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    private String ndkHome = System.getenv("ANDROID_NDK_HOME");
    private String target;
    private String apiLevel;
    private String sysroot;
    private String archiver;

    public AndroidTarget(Target target, ApiLevel apiLevel) {
        this(SourceLanguage.CPP, target, apiLevel);
    }

    public AndroidTarget(SourceLanguage language, Target target, ApiLevel apiLevel) {
        cppCompiler.clear();
        linkerCompiler.clear();

        if(ndkHome == null) {
            return;
        }
        this.libPrefix = "lib";

        String osFolder = "windows-x86_64";
        if(isUnix()) {
            osFolder = "linux-x86_64";
        }
        else if(isMac()) {
            // TODO Verify if this is correct
            osFolder = "mac-x86_64";
        }
        String toolchain = ndkHome + "/toolchains/llvm/prebuilt/" + osFolder;
        if(language == SourceLanguage.C) {
            String compiler = toolchain + "/bin/clang";
            cppCompiler.add(compiler);
            linkerCompiler.add(compiler);
        }
        else if(language == SourceLanguage.CPP) {
            String compiler = toolchain + "/bin/clang++";
            cppCompiler.add(compiler);
            linkerCompiler.add(compiler);
        }

        archiver = toolchain + "/bin/llvm-ar"; // Archiver for static libraries
        sysroot = toolchain + "/sysroot"; // System root for Android libraries

        // Target settings
        this.target = target.value;
        this.apiLevel = apiLevel.value; // Android API level (e.g., Android 10)

        String targetPath = target.folder;

        this.libDirSuffix = "android/" + targetPath + "/";
        this.tempBuildDir = "target/" + targetPath + "/";

        cppCompiler.add("--target=" + this.target + this.apiLevel);
        cppCompiler.add("--sysroot=" + sysroot);
        cppCompiler.add("-fPIC");
        cppFlags.add("-O2");
        cppFlags.add("-Wall");
        cppFlags.add("-D__ANDROID__");
        cppFlags.add("-fvisibility=hidden");

        cppFlags.add("-c");
        libSuffix = "64.o";
    }

    @Override
    protected void setup(BuildConfig config) {
        if(ndkHome == null) {
            return;
        }

        if(isStatic) {
            linkerCompiler.clear();
            linkerCompiler.add(archiver);
            String staticLib = "libmystaticlib.a";
            linkerFlags.add("rcs");
            libSuffix = ".a";
        }
        else {
            linkerFlags.add("-lm");
            linkerFlags.add("--target=" + target + apiLevel);
            linkerFlags.add("--sysroot=" + sysroot);
            linkerFlags.add("-shared");
            linkerFlags.add("-static-libstdc++"); // Statically link C++ runtime
            libSuffix = ".so";
        }
    }

    @Override
    protected void onLink(ArrayList<CustomFileDescriptor> compiledObject, String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(compiledObject, objFilePath, libPath);
        }
    }

    public void addJNIHeaders() {
        headerDirs.add("-Ijni-headers/");
        headerDirs.add("-Ijni-headers/linux");
        headerDirs.add("-Ijni-headers/win32");
        headerDirs.add("-Ijni-headers/mac");
    }

    public enum Target {
        arm64_v8a("aarch64-linux-android", "arm64-v8a"),
        armeabi_v7a("armv7a-linux-androideabi", "armeabi-v7a"),
        x86_64("x86_64-linux-android", "x86_64"),
        x86("i686-linux-android", "x86");

        private String value;
        private String folder;

        Target(String value, String folder) {
            this.value = value;
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }

    }

    public enum ApiLevel {
        Android_16_36("36"),
        Android_15_35("35"),
        Android_14_34("34"),
        Android_13_33("33"),
        Android_12_32("32"),
        Android_12_31("31"),
        Android_11_30("30"),
        Android_10_29("29"),
        Android_09_28("28");

        private String value;

        ApiLevel(String value) {
            this.value = value;
        }
    }
}
