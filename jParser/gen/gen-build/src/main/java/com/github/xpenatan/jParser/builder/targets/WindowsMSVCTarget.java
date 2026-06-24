package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class WindowsMSVCTarget extends DefaultBuildTarget {

    public static boolean DEBUG_BUILD;

    // https://learn.microsoft.com/en-us/cpp/build/reference/compiler-options-listed-by-category?view=msvc-170

    public WindowsMSVCTarget() {
        this.libDirSuffix = "windows/vc/";
        this.tempBuildDir = "target/windows/vc/";
        linkObjSuffix = "**.obj";

        cppCompiler.clear();
        linkerCompiler.clear();

        String vcvarsall = findVcvarsall();
        addMsvcSetup(cppCompiler, vcvarsall);
        cppCompiler.add("cl");

        addMsvcSetup(linkerCompiler, vcvarsall);

        compilerOutputCommand = "-Fo:";
        cppFlags.add("-c");
        if(DEBUG_BUILD) {
            cppFlags.add("/Z7"); // add debug information in .obj to work in visual studio
            cppFlags.add("/Od");
        }
        else {
            cppFlags.add("/O2");
        }
        linkerOutputCommand = "/OUT:";
        libSuffix = "64.dll";
    }

    @Override
    protected void setup(BuildConfig config) {
        if(isStatic) {
            linkerCompiler.add("lib");
            libSuffix = "64_.lib";
        }
        else {
            linkerCompiler.add("link");
            if(DEBUG_BUILD) {
                linkerCompiler.add("/DEBUG"); // Generates .pbd file
            }
        }
        linkerCompiler.add("/NOLOGO");
        linkerCompiler.add("/MACHINE:X64");
    }

    @Override
    protected boolean compile(BuildConfig config, CustomFileDescriptor buildTargetTemp, ArrayList<CustomFileDescriptor> cppFiles) {
        boolean multiCoreCompile = this.multiCoreCompile;
        this.multiCoreCompile = false;
        if(multiCoreCompile) {
            // Use native MSVC multi core support
            cppCompiler.add("/MP");
        }
        boolean compile = super.compile(config, buildTargetTemp, cppFiles);
        this.multiCoreCompile = multiCoreCompile;
        return compile;
    }

    private static void addMsvcSetup(ArrayList<String> command, String vcvarsall) {
        command.add("cmd");
        command.add("/d");
        command.add("/c");
        command.add("call");
        command.add(vcvarsall);
        command.add("x64");
        command.add("&&");
    }

    private static String findVcvarsall() {
        String override = firstExistingFile(
                System.getProperty("jparser.vcvarsall"),
                System.getenv("JPARSER_VCVARSALL"),
                System.getenv("VCVARSALL_PATH")
        );
        if(override != null) {
            return override;
        }

        String pathCommand = findOnPath("vcvarsall.bat");
        if(pathCommand != null) {
            return pathCommand;
        }

        String envPath = firstExistingFile(
                child(System.getenv("VCINSTALLDIR"), "Auxiliary\\Build\\vcvarsall.bat"),
                child(System.getenv("VSINSTALLDIR"), "VC\\Auxiliary\\Build\\vcvarsall.bat")
        );
        if(envPath != null) {
            return envPath;
        }

        String vswherePath = findVswhere();
        if(vswherePath != null) {
            String installPath = runVswhere(vswherePath);
            String detectedPath = firstExistingFile(child(installPath, "VC\\Auxiliary\\Build\\vcvarsall.bat"));
            if(detectedPath != null) {
                return detectedPath;
            }
        }

        return "vcvarsall";
    }

    private static String firstExistingFile(String... paths) {
        for(String path : paths) {
            if(path == null || path.trim().isEmpty()) {
                continue;
            }
            File file = new File(path);
            if(file.isFile()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    private static String child(String parent, String child) {
        if(parent == null || parent.trim().isEmpty()) {
            return null;
        }
        return new File(parent, child).getPath();
    }

    private static String findOnPath(String executable) {
        String path = System.getenv("PATH");
        if(path == null || path.trim().isEmpty()) {
            return null;
        }
        String[] dirs = path.split(File.pathSeparator);
        for(String dir : dirs) {
            String found = firstExistingFile(child(dir, executable));
            if(found != null) {
                return found;
            }
        }
        return null;
    }

    private static String findVswhere() {
        String pathCommand = findOnPath("vswhere.exe");
        if(pathCommand != null) {
            return pathCommand;
        }
        return firstExistingFile(
                child(System.getenv("ProgramFiles(x86)"), "Microsoft Visual Studio\\Installer\\vswhere.exe"),
                child(System.getenv("ProgramFiles"), "Microsoft Visual Studio\\Installer\\vswhere.exe")
        );
    }

    private static String runVswhere(String vswherePath) {
        try {
            Process process = new ProcessBuilder(
                    vswherePath,
                    "-latest",
                    "-products",
                    "*",
                    "-requires",
                    "Microsoft.VisualStudio.Component.VC.Tools.x86.x64",
                    "-property",
                    "installationPath"
            ).redirectErrorStream(true).start();

            String installPath = null;
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null) {
                    if(!line.trim().isEmpty()) {
                        installPath = line.trim();
                        break;
                    }
                }
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if(!finished) {
                process.destroyForcibly();
                return null;
            }
            if(process.exitValue() == 0) {
                return installPath;
            }
        }
        catch(Exception ignored) {
        }
        return null;
    }
}
