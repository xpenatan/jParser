package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;

public class BuildCpp {
    public static boolean DEBUG_BUILD = false;

    public static void build(String libName, String projectPath, String sharedLibBaseProject) {
        String sharedSrcPath = sharedLibBaseProject + "/jni/src/";
        String[] headerDir = {"src", sharedSrcPath};
        String[] includes = {"**/*.cpp"};

        BuildConfig buildConfig = new BuildConfig(libName, "target", "/libs", projectPath + "/jni");
        buildConfig.sharedLibs = new String[3];

        new AntScriptGenerator().generate(buildConfig,
                genWindows(buildConfig, sharedLibBaseProject, headerDir, includes),
                genLinux(buildConfig, sharedLibBaseProject, headerDir, includes),
                genMac(buildConfig, sharedLibBaseProject, headerDir, includes));

        if(isWindows() || isUnix()) {
            if(!BuildExecutor.executeAnt(projectPath + "/jni/build-windows64.xml", "-v", "-Dhas-compiler=true", "postcompile"))
                throw new RuntimeException();
        }
        if(isUnix()) {
            if(!BuildExecutor.executeAnt(projectPath + "/jni/build-linux64.xml", "-v", "-Dhas-compiler=true", "postcompile"))
                throw new RuntimeException();
        }
        if(isMac()) {
            if(!BuildExecutor.executeAnt(projectPath + "/jni/build-macosx64.xml", "-v", "-Dhas-compiler=true"))
                throw new RuntimeException();
        }
        if(!BuildExecutor.executeAnt(projectPath + "/jni/build.xml", "-v", "pack-natives"))
            throw new RuntimeException();
    }

    private static BuildTarget genWindows(BuildConfig buildConfig, String sharedLibBaseProject, String[] headerDir, String[] includes) {
        String libFolder = sharedLibBaseProject + "/libs/windows64";

        BuildTarget win64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, true);

        win64.cppIncludes = includes;
        win64.headerDirs = headerDir;
        win64.linkerFlags = "-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64";
        win64.libraries = "-L" + libFolder + " -limgui-cpp64";
        win64.excludeFromMasterBuildFile = true;
        win64.cppFlags += " -std=c++11";
        buildConfig.sharedLibs[0] = libFolder;
        if(DEBUG_BUILD)
            win64.cppFlags = "-c -Wall -O0 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -g";
        return win64;
    }

    private static BuildTarget genLinux(BuildConfig buildConfig, String sharedLibBaseProject, String[] headerDir, String[] includes) {
        String libFolder = sharedLibBaseProject + "/libs/linux64";

        BuildTarget lin64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Linux, true);
        lin64.cppIncludes = includes;
        lin64.headerDirs = headerDir;
        lin64.libraries = "-L" + libFolder + " -limgui-cpp64";
        lin64.excludeFromMasterBuildFile = true;
        buildConfig.sharedLibs[1] = libFolder;
        return lin64;
    }

    private static BuildTarget genMac(BuildConfig buildConfig, String sharedLibBaseProject, String[] headerDir, String[] includes) {
        String libFolder = sharedLibBaseProject + "/libs/macosx64";

        BuildTarget mac64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.MacOsX, true);
        mac64.cppIncludes = includes;
        mac64.headerDirs = headerDir;
        // for some weird reason adding -v stop getting errors with github actions
        mac64.linkerFlags = "-v -shared -arch x86_64 -mmacosx-version-min=10.7 -stdlib=libc++";
        mac64.libraries = "-L" + libFolder + " -limgui-cpp64";
        mac64.excludeFromMasterBuildFile = true;
        buildConfig.sharedLibs[2] = libFolder;
        mac64.cppFlags += " -std=c++11";
        return mac64;
    }

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }
}
