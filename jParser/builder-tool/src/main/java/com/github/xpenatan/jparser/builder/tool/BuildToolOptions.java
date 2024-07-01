package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.builder.BuildTarget;
import java.io.File;
import java.io.IOException;

public class BuildToolOptions {

    public String libName;
    public String libBasePackage;
    public String libBasePath;
    public String libBuildPath;
    public String libBuildCPPPath;
    public String libCorePath;
    public String libTeavmPath;
    public String idlPath;
    public String libBaseJavaDir;
    public String cppSourceDir;
    public String customSourceDir;
    public String libsDir;
    public String cppDestinationPath;
    public String libDestinationPath;

    public boolean windows64;
    public boolean linux64;
    public boolean mac64;
    public boolean macArm;
    public boolean android;
    public boolean iOS;
    public boolean teavm;

    /**
     *
     * @param libName
     * @param libBasePackage
     * @param buildSourceDir path inside lib-build module
     * @param platform windows64, linux64, mac64, mac64arm, android, ios, teavm
     */
    public BuildToolOptions(String libName, String libBasePackage, String buildSourceDir, String ... platform) {
        this.libName = libName;
        this.libBasePackage = libBasePackage;

        String libPath = null;
        try {
            libPath = new File("./../").getCanonicalPath().replace("\\", "/");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        libBasePath = libPath + "/lib-base";
        libBuildPath = libPath + "/lib-build";
        libCorePath = libPath + "/lib-core";
        libTeavmPath = libPath + "/lib-teavm";

        idlPath = libBuildPath + "/src/main/cpp/" + libName + ".idl";

        libBaseJavaDir = libBasePath + "/src/main/java";

        cppSourceDir = libBuildPath + buildSourceDir;
        customSourceDir = libBuildPath + "/src/main/cpp/custom";

        libBuildCPPPath = libBuildPath + "/build/c++";
        libsDir = libBuildCPPPath + "/libs";
        cppDestinationPath = libBuildCPPPath + "/src";
        libDestinationPath = cppDestinationPath + "/" + libName;

        for(int i = 0; i < platform.length; i++) {
            String arg = platform[i];
            if(arg.equals("windows64") && (BuildTarget.isWindows() || BuildTarget.isUnix())) {
                windows64 = true;
            }
            else if(arg.equals("linux64") && BuildTarget.isUnix()) {
                linux64 = true;
            }
            else if(arg.equals("mac64") && BuildTarget.isMac()) {
                mac64 = true;
            }
            else if(arg.equals("macArm") && BuildTarget.isMac()) {
                macArm = true;
            }
            else if(arg.equals("android")) {
                android = true;
            }
            else if(arg.equals("ios") && BuildTarget.isMac()) {
                iOS = true;
            }
            else if(arg.equals("teavm")) {
                teavm = true;
            }
        }
    }
}