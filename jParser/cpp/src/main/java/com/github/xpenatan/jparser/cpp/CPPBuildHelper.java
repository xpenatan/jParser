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

    public static void build(String libName, String buildPath) {
        build(libName, buildPath, null, null, null, false);
    }

    public static void build(String libName, String buildPath, String [] cppFlags) {
        build(libName, buildPath, cppFlags, null, null, false);
    }

    public static void build(String libName, String buildPath, String[] cppFlags, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        build(libName, buildPath, "libs", cppFlags, sharedLibBaseProject, "/src/", sharedLibName, includeToJar);
    }

    public static void build(String libName, String buildPath, String libsDir, String[] cppFlags, String sharedLibBaseProject, String sourceFolder, String sharedLibName, boolean includeToJar) {
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
        String[] headerDir = {"src", sharedSrcPath};
        String[] includes = {"**/*.cpp"};

        BuildConfig buildConfig = new BuildConfig(libName, "target", libsDir, buildPath);

        boolean isWindows = isWindows();
        boolean isUnix = isUnix();
        boolean isMac = isMac();

        if(sharedLibName != null) {
            buildConfig.sharedLibs = new String[3];
        }

        BuildTarget windowTarget = genWindows(buildConfig, headerDir, includes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget linuxTarget = genLinux(buildConfig, headerDir, includes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
        BuildTarget macTarget = genMac(libName, buildConfig, headerDir, includes, cppFlags, sharedLibBaseProject, sharedLibName, includeToJar);
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

    private static BuildTarget genWindows(BuildConfig buildConfig, String[] headerDir, String[] includes, String[] cppFlags, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/windows64";
        }
        BuildTarget win64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, true);

        win64.cppIncludes = includes;
        win64.headerDirs = headerDir;
        win64.linkerFlags = "-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64";
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
        if(DEBUG_BUILD)
            win64.cppFlags = "-c -Wall -O0 -mfpmath=sse -msse2 -fmessage-length=0 -m64 -g";
        win64.cppFlags += " -std=c++11";
        return win64;
    }

    private static BuildTarget genLinux(BuildConfig buildConfig, String[] headerDir, String[] includes, String[] cppFlags, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
        String libFolder = null;
        if(sharedLibBaseProject != null) {
            libFolder = sharedLibBaseProject + "/libs/linux64";
        }

        BuildTarget lin64 = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Linux, true);
        lin64.cppIncludes = includes;
        lin64.headerDirs = headerDir;
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

    private static BuildTarget genMac(String libName, BuildConfig buildConfig, String[] headerDir, String[] includes, String[] cppFlags, String sharedLibBaseProject, String sharedLibName, boolean includeToJar) {
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
}
