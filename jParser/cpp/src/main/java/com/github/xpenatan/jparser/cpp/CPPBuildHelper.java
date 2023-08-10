package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.CustomAntScriptGenerator;
import com.badlogic.gdx.jnigen.FileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CPPBuildHelper {
    public static boolean DEBUG_BUILD = false;

    public static void build(Config config) {
        String libName = config.libName;
        String buildPath = config.buildPath;
        String libsDir = config.libsDir;
        String[] cppFlags = config.cppFlags;
        String sharedLibBaseProject = config.sharedLibBaseProject;
        String sourceFolder = config.sourceFolder;
        String sharedLibName = config.sharedLibName;
        boolean includeToJar = config.includeToJar;

        String sharedSrcPath = null;
        try {
            buildPath = buildPath.replace("\\", File.separator);
            buildPath = new File(buildPath).getCanonicalPath();
            if(sharedLibBaseProject != null) {
                sharedLibBaseProject = sharedLibBaseProject.replace("\\", File.separator);
                sharedLibBaseProject = new File(sharedLibBaseProject).getCanonicalPath();
                sharedSrcPath = sharedLibBaseProject + sourceFolder;
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        String[] headerDirs = null;
        String[] cppIncludes = null;
        String[] cIncludes = null;

        if(sharedSrcPath != null) {
            config.headerDir.add(sharedSrcPath);
        }
        headerDirs = new String[config.headerDir.size()];
        config.headerDir.toArray(headerDirs);
        cppIncludes = new String[config.cppIncludes.size()];
        config.cppIncludes.toArray(cppIncludes);
        cIncludes = new String[config.cIncludes.size()];
        config.cIncludes.toArray(cIncludes);

        BuildConfig buildConfig = new BuildConfig(libName, "target", libsDir, buildPath);

        boolean isWindows = isWindows();
        boolean isUnix = isUnix();
        boolean isMac = isMac();

        if(sharedLibName != null) {
            buildConfig.sharedLibs = new String[3];
        }

        BuildTarget windowTarget = genWindows(buildConfig, headerDirs, cIncludes, cppIncludes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget linuxTarget = genLinux(buildConfig, headerDirs, cIncludes, cppIncludes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget macTarget = genMac(libName, buildConfig, headerDirs, cIncludes, cppIncludes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
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
                isValid = executeAnt(buildPath + "/build-windows64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.Linux) {
                isValid = executeAnt(buildPath + "/build-linux64.xml", "-v", "-Dhas-compiler=true", "postcompile");
            }
            else if(target.os == BuildTarget.TargetOs.MacOsX) {
                isValid = executeAnt(buildPath + "/build-macosx64.xml", "-v", "-Dhas-compiler=true");
            }
            if(!isValid) {
                throw new RuntimeException();
            }
        }
        if(!executeAnt(buildPath + "/build.xml", "-v", "pack-natives"))
            throw new RuntimeException();
    }

    private static BuildTarget genWindows(
            BuildConfig buildConfig,
            String[] headerDirs,
            String[] cIncludes,
            String[] cppIncludes,
            String[] cppFlags,
            String sharedLibBaseProject,
            String sharedLibName,
            boolean includeToJar
    ) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/windows64";
        }
        BuildTarget win64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, true);
        win64.cIncludes = cIncludes;
        win64.cppIncludes = cppIncludes;
        win64.headerDirs = headerDirs;
        win64.linkerFlags = "-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64";

        if(DEBUG_BUILD)
            win64.cppFlags = " -c -Wall -O0 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -g";

        if(cppFlags != null) {
            for(String cppFlag : cppFlags) {
                win64.cppFlags += cppFlag;
            }
        }
//        win64.excludeFromMasterBuildFile = true;
        if(libFolder != null) {
            win64.libraries = "-L" + libFolder + " -l" + sharedLibName;
            if(includeToJar) {
                buildConfig.sharedLibs[0] = libFolder;
            }
        }
        win64.cppFlags += " -std=c++11";
        return win64;
    }

    private static BuildTarget genLinux(
            BuildConfig buildConfig,
            String[] headerDirs,
            String[] cIncludes,
            String[] cppIncludes,
            String[] cppFlags,
            String sharedLibBaseProject,
            String sharedLibName,
            boolean includeToJar
    ) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/linux64";
        }

        BuildTarget lin64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Linux, true);
        lin64.cIncludes = cIncludes;
        lin64.cppIncludes = cppIncludes;
        lin64.headerDirs = headerDirs;
        if(cppFlags != null) {
            for(String cppFlag : cppFlags) {
                lin64.cppFlags += cppFlag;
            }
        }
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

    private static BuildTarget genMac(
            String libName,
            BuildConfig buildConfig,
            String[] headerDirs,
            String[] cIncludes,
            String[] cppIncludes,
            String[] cppFlags,
            String sharedLibBaseProject,
            String sharedLibName,
            boolean includeToJar
    ) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/macosx64";
        }

        BuildTarget mac64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.MacOsX, true);
        mac64.cIncludes = cIncludes;
        mac64.cppIncludes = cppIncludes;
        mac64.headerDirs = headerDirs;
        // for some weird reason adding -v stop getting errors with github actions
        mac64.linkerFlags = "-v -shared -arch x86_64 -mmacosx-version-min=10.7 -stdlib=libc++";

        String libFilename = mac64.getSharedLibFilename(libName);
        mac64.linkerFlags += " -install_name @rpath/" + libFilename;
        if(cppFlags != null) {
            for(String cppFlag : cppFlags) {
                mac64.cppFlags += cppFlag;
            }
        }
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

    public static boolean executeAnt (String buildFile, String... params) {
        FileDescriptor build = new FileDescriptor(buildFile);
        String ant = System.getProperty("os.name").contains("Windows") ? "ant.bat" : "ant";

        List<String> command = new ArrayList<>();
        command.add(ant);
        command.add("-f");
        command.add(build.file().getAbsolutePath());
        command.addAll(Arrays.asList(params));

        String[] args = command.toArray(new String[0]);
        System.out.println("Executing '" + command + "'");
        return startProcess(build.parent().file(), args);
    }

    public static boolean startProcess (File directory, String command) {
        String[] commands = command.split(" ");
        return startProcess(directory, commands);
    }

    public static boolean startProcess (File directory, String... commands) {
        try {
            System.out.println("Commands: " + Arrays.toString(commands));
            final Process process = new ProcessBuilder(commands)
                    .redirectErrorStream(true)
                    .directory(new File(System.getProperty("user.home")))
                    .start();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run () {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            // augment output with java file line references :D
                            printFileLineNumber(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private void printFileLineNumber (String line) {
                    System.out.println(line);
                }
            });
            t.setDaemon(true);
            t.start();
            process.waitFor();
            t.join();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class Config {
        public String libName;
        public String buildPath;
        public String libsDir = "libs";
        public final ArrayList<String> headerDir = new ArrayList<>();
        public final ArrayList<String> cppIncludes = new ArrayList<>();
        public final ArrayList<String> cIncludes = new ArrayList<>();
        public String[] cppFlags;
        public String sharedLibBaseProject;
        public String sourceFolder = "/src/";
        public String sharedLibName;
        public boolean includeToJar;
    }
}
