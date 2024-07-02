package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.builder.BuildTarget;
import java.io.File;
import java.io.IOException;

public class BuildToolOptions {
    public String libPath;
    public String libName;
    public String libBasePackage;
    public String buildSourceDir;
    public boolean generateTeaVM = true;
    public boolean generateCPP = true;

    /** Name of the idl file located in [Module Build Path] + src/main/cpp/myidl.idl. The default is libName but can be changed. */
    public String idlName;

    private String modulePrefix;
    private String moduleBasePath;
    private String moduleBuildPath;
    private String moduleBuildCPPPath;
    private String moduleCorePath;
    private String moduleTeavmPath;
    private String idlPath;
    private String moduleBaseJavaDir;
    private String cppSourceDir;
    private String customSourceDir;
    private String libsDir;
    private String cppDestinationPath;
    private String libDestinationPath;

    public final boolean windows64;
    public final boolean linux64;
    public final boolean mac64;
    public final boolean macArm;
    public final boolean android;
    public final boolean iOS;
    public final boolean teavm;

    /**
     *
     * @param libName module name
     * @param libBasePackage module package that all classes will be in
     * @param modulePrefix module prefix name. ex: imgui. So it will be imgui-core, imgui-teavm, etc.
     * @param buildSourceDir path inside lib-build module
     * @param platform windows64, linux64, mac64, mac64arm, android, ios, teavm
     */
    public BuildToolOptions(String libName, String libBasePackage, String modulePrefix, String buildSourceDir, String ... platform) {
        this.libName = libName;
        this.libBasePackage = libBasePackage;
        this.modulePrefix = modulePrefix;
        this.buildSourceDir = buildSourceDir;
        this.idlName = libName;

        boolean windows64tmp = false;
        boolean linux64tmp = false;
        boolean mac64tmp = false;
        boolean macArmtmp = false;
        boolean androidtmp = false;
        boolean iOStmp = false;
        boolean teavmtmp = false;
        for(int i = 0; i < platform.length; i++) {
            String arg = platform[i];
            if(arg.equals("windows64") && (BuildTarget.isWindows() || BuildTarget.isUnix())) {
                windows64tmp = true;
            }
            else if(arg.equals("linux64") && BuildTarget.isUnix()) {
                linux64tmp = true;
            }
            else if(arg.equals("mac64") && BuildTarget.isMac()) {
                mac64tmp = true;
            }
            else if(arg.equals("macArm") && BuildTarget.isMac()) {
                macArmtmp = true;
            }
            else if(arg.equals("android")) {
                androidtmp = true;
            }
            else if(arg.equals("ios") && BuildTarget.isMac()) {
                iOStmp = true;
            }
            else if(arg.equals("teavm")) {
                teavmtmp = true;
            }
        }
        windows64 = windows64tmp;
        linux64 = linux64tmp;
        mac64 = mac64tmp;
        macArm = macArmtmp;
        android = androidtmp;
        iOS = iOStmp;
        teavm = teavmtmp;
    }

    void setup() {
        if(libPath == null) {
            try {
                libPath = new File("./../").getCanonicalPath().replace("\\", "/");
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        moduleBasePath = libPath + "/" + modulePrefix + "-base";
        moduleBuildPath = libPath + "/" + modulePrefix + "-build";
        moduleCorePath = libPath + "/" + modulePrefix + "-core";
        moduleTeavmPath = libPath + "/" + modulePrefix + "-teavm";

        moduleBaseJavaDir = moduleBasePath + "/src/main/java";
        idlPath = moduleBuildPath + "/src/main/cpp/" + idlName + ".idl";
        cppSourceDir = moduleBuildPath + buildSourceDir;
        customSourceDir = moduleBuildPath + "/src/main/cpp/custom";

        moduleBuildCPPPath = moduleBuildPath + "/build/c++";
        libsDir = moduleBuildCPPPath + "/libs";
        cppDestinationPath = moduleBuildCPPPath + "/src";
        libDestinationPath = cppDestinationPath + "/" + libName;
    }

    public String getModulePrefix() {
        return modulePrefix;
    }

    public String getModuleBasePath() {
        return moduleBasePath;
    }

    public String getModuleBuildPath() {
        return moduleBuildPath;
    }

    public String getModuleBuildCPPPath() {
        return moduleBuildCPPPath;
    }

    public String getModuleCorePath() {
        return moduleCorePath;
    }

    public String getModuleTeaVMPath() {
        return moduleTeavmPath;
    }

    public String getIDLPath() {
        return idlPath;
    }

    public String getModuleBaseJavaDir() {
        return moduleBaseJavaDir;
    }

    public String getCPPSourceDir() {
        return cppSourceDir;
    }

    /**
     * Custom code always points to [Module Build Path] + src/main/cpp/custom
     */
    public String getCustomSourceDir() {
        return customSourceDir;
    }

    public String getLibsDir() {
        return libsDir;
    }

    public String getCPPDestinationPath() {
        return cppDestinationPath;
    }

    public String getLibDestinationPath() {
        return libDestinationPath;
    }
}