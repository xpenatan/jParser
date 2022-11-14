package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CPPBuildHelper {
    public static boolean DEBUG_BUILD = false;

    public static void build(String libName, String projectPath) {
        build(libName, projectPath, null, null);
    }

    public static void build(String libName, String projectPath, String sharedLibBaseProject, String sharedLibName) {
        String sharedSrcPath = null;
        try {
            projectPath = new File(projectPath).getCanonicalPath();
            if(sharedLibBaseProject != null) {
                sharedLibBaseProject = new File(sharedLibBaseProject).getCanonicalPath();
                sharedSrcPath = sharedLibBaseProject + "/jni/src/";
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        String[] headerDir = {"src", sharedSrcPath};
        String[] includes = {"**/*.cpp"};

        BuildConfig buildConfig = new BuildConfig(libName, "target", "libs", projectPath + "/jni");

        boolean isWindows = isWindows();
        boolean isUnix = isUnix();
        boolean isMac = isMac();
        ArrayList<BuildTarget> targets = new ArrayList<>();
        if(isWindows || isUnix) {
            if(isUnix) {
                if(sharedLibName != null) {
                    buildConfig.sharedLibs = new String[2];
                }
                targets.add(genLinux(buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName));
            }
            else {
                if(sharedLibName != null) {
                    buildConfig.sharedLibs = new String[1];
                }
            }
            targets.add(genWindows(buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName));
        }
        else if(isMac) {
            if(sharedLibName != null) {
                buildConfig.sharedLibs = new String[1];
            }
            targets.add(genMac(buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName));
        }

        new AntScriptGenerator().generate(buildConfig, targets.toArray(new BuildTarget[targets.size()]));

        for(int i = 0; i < targets.size(); i++) {
            BuildTarget target = targets.get(i);
            boolean isValid = false;
            if(target.os == BuildTarget.TargetOs.Windows) {
                isValid = BuildExecutor.executeAnt(projectPath + "/jni/build-windows64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.Linux) {
                isValid = BuildExecutor.executeAnt(projectPath + "/jni/build-linux64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.MacOsX) {
                isValid = BuildExecutor.executeAnt(projectPath + "/jni/build-macosx64.xml", "-v", "-Dhas-compiler=true");
            }
            if(!isValid) {
                throw new RuntimeException();
            }
        }
        if(!BuildExecutor.executeAnt(projectPath + "/jni/build.xml", "-v", "pack-natives"))
            throw new RuntimeException();
    }

    private static BuildTarget genWindows(BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/windows64";
        }
        BuildTarget win64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, true);

        win64.cppIncludes = includes;
        win64.headerDirs = headerDir;
        win64.linkerFlags = "-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64";
//        win64.excludeFromMasterBuildFile = true;
        win64.cppFlags += " -std=c++11";
        if(libFolder != null) {
            win64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            buildConfig.sharedLibs[0] = libFolder;
        }
        if(DEBUG_BUILD)
            win64.cppFlags = "-c -Wall -O0 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -g";
        return win64;
    }

    private static BuildTarget genLinux(BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/linux64";
        }

        BuildTarget lin64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Linux, true);
        lin64.cppIncludes = includes;
        lin64.headerDirs = headerDir;
//        lin64.excludeFromMasterBuildFile = true;
        if(libFolder != null) {
            lin64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            buildConfig.sharedLibs[1] = libFolder;
        }
        return lin64;
    }

    private static BuildTarget genMac(BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/macosx64";
        }

        BuildTarget mac64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.MacOsX, true);
        mac64.cppIncludes = includes;
        mac64.headerDirs = headerDir;
        // for some weird reason adding -v stop getting errors with github actions
        mac64.linkerFlags = "-v -shared -arch x86_64 -mmacosx-version-min=10.7 -stdlib=libc++";
//        mac64.excludeFromMasterBuildFile = true;
        if(libFolder != null) {
            mac64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            buildConfig.sharedLibs[0] = libFolder;
        }
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
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("Linux"));
    }
}
