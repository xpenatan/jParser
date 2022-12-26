package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.CustomAntScriptGenerator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CPPBuildHelper {
    public static boolean DEBUG_BUILD = false;

    public static void build(String libName, String projectPath) {
        build(libName, projectPath, null, null, false);
    }

    public static void build(String libName, String projectPath, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        build(libName, projectPath, "libs", sharedLibBaseProject, "/src/", sharedLibName, includeToJar);
    }

    public static void build(String libName, String projectPath, String libsDir, String sharedLibBaseProject, String sourceFolder, String sharedLibName, boolean includeToJar) {
        String sharedSrcPath = null;
        try {
            projectPath = projectPath.replace("\\", File.separator);
            projectPath = new File(projectPath).getCanonicalPath();
            if(sharedLibBaseProject != null) {
                sharedLibBaseProject = sharedLibBaseProject.replace("\\", File.separator);
                sharedLibBaseProject = new File(sharedLibBaseProject).getCanonicalPath();
                sharedSrcPath = sharedLibBaseProject + sourceFolder;
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        String[] headerDir = {"src", sharedSrcPath};
        String[] includes = {"**/*.cpp"};

        BuildConfig buildConfig = new BuildConfig(libName, "target", libsDir, projectPath);

        boolean isWindows = isWindows();
        boolean isUnix = isUnix();
        boolean isMac = isMac();

        if(sharedLibName != null) {
            buildConfig.sharedLibs = new String[3];
        }

        BuildTarget windowTarget = genWindows(buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget linuxTarget = genLinux(buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget macTarget = genMac(libName, buildConfig, headerDir, includes, sharedLibBaseProject, sharedLibName, includeToJar);
        new CustomAntScriptGenerator().generate(buildConfig,
                windowTarget,
                linuxTarget,
                macTarget);

        ArrayList<BuildTarget> targets = new ArrayList<>();
        if(isWindows || isUnix) {
            if(isUnix) {
                targets.add(linuxTarget);
            }
            targets.add(windowTarget);
        }
        else if(isMac) {
            targets.add(macTarget);
        }

        for(int i = 0; i < targets.size(); i++) {
            BuildTarget target = targets.get(i);
            boolean isValid = false;
            if(target.os == BuildTarget.TargetOs.Windows) {
                isValid = BuildExecutor.executeAnt(projectPath + "/build-windows64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.Linux) {
                isValid = BuildExecutor.executeAnt(projectPath + "/build-linux64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.MacOsX) {
                isValid = BuildExecutor.executeAnt(projectPath + "/build-macosx64.xml", "-v", "-Dhas-compiler=true");
            }
            if(!isValid) {
                throw new RuntimeException();
            }
        }
        if(!BuildExecutor.executeAnt(projectPath + "/build.xml", "-v", "pack-natives"))
            throw new RuntimeException();
    }

    private static BuildTarget genWindows(BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/windows64";
        }
        BuildTarget win64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, true);

        win64.cppIncludes = includes;
        win64.headerDirs = headerDir;
        win64.linkerFlags = "-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64";
//        win64.excludeFromMasterBuildFile = true;
        if(libFolder != null) {
            win64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            if(includeToJar) {
                buildConfig.sharedLibs[0] = libFolder;
            }
        }
        if(DEBUG_BUILD)
            win64.cppFlags = "-c -Wall -O0 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -g";
        win64.cppFlags += " -std=c++11";
        return win64;
    }

    private static BuildTarget genLinux(BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
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
            if(includeToJar) {
                buildConfig.sharedLibs[1] = libFolder;
            }
            lin64.linkerFlags += ",-rpath,'$ORIGIN'";
        }
        return lin64;
    }

    private static BuildTarget genMac(String libName, BuildConfig buildConfig, String[] headerDir, String[] includes, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/macosx64";
        }

        BuildTarget mac64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.MacOsX, true);

        String libFilename = mac64.getSharedLibFilename(libName);

        mac64.cppIncludes = includes;
        mac64.headerDirs = headerDir;
        // for some weird reason adding -v stop getting errors with github actions
        mac64.linkerFlags = "-v -shared -arch x86_64 -mmacosx-version-min=10.7 -stdlib=libc++";

        mac64.linkerFlags += " -install_name @rpath/" + libFilename;
//        mac64.excludeFromMasterBuildFile = true;
        if(libFolder != null) {
            mac64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            if(includeToJar) {
                buildConfig.sharedLibs[2] = libFolder;
            }
            mac64.linkerFlags += " -Wl,-rpath,@loader_path/. -Wl,-rpath,@executable_path/.";
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
