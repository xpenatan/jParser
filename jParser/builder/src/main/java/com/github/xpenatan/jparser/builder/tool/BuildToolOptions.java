package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BuildToolOptions {
    public String libPath;
    public String libName;
    public String moduleName;
    public String libBasePackage;
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
    private ArrayList<String> idlPath = new ArrayList<>();
    private ArrayList<String> idlPathRef = new ArrayList<>();
    private ArrayList<String> additionalSourceDirs = new ArrayList<>();
    private String moduleBaseJavaDir;
    private String sourcePath;
    private String customSourceDir;
    private String libsDir;
    private String cppDestinationPath;

    private String[] args;

    /**
     *
     * @param libName module name
     * @param libBasePackage module package that all classes will be in
     * @param modulePrefix module prefix name. ex: imgui. So it will be imgui-core, imgui-teavm, etc.
     * @param cppSourcePath full path where the source is located
     * @param args windows64, linux64, mac64, mac64arm, android, ios, teavm
     */
    public BuildToolOptions(String libName, String libBasePackage, String modulePrefix, String cppSourcePath, String ... args) {
        this.libName = libName;
        this.libBasePackage = libBasePackage;
        this.modulePrefix = modulePrefix;
        this.args = args;

        if(cppSourcePath != null) {
            boolean exists = new CustomFileDescriptor(cppSourcePath).exists();
            if(exists) {
                this.sourcePath = cppSourcePath.replace("\\", "/");
            }
            else {
                try {
                    this.sourcePath = new File(".", cppSourcePath).getCanonicalPath().replace("\\", "/");
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(!this.sourcePath.endsWith("/")) {
                this.sourcePath += "/";
            }
        }
        this.idlName = libName;
        this.moduleName = libName;
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
        String idlPathItem = moduleBuildPath + "/src/main/cpp/" + idlName + ".idl";
        idlPath.add(idlPathItem);
        customSourceDir = moduleBuildPath + "/src/main/cpp/custom/";

        moduleBuildCPPPath = moduleBuildPath + "/build/c++";
        libsDir = moduleBuildCPPPath + "/libs";
        cppDestinationPath = moduleBuildCPPPath + "/src";
    }

    public boolean containsArg(String arg) {
        for(int i = 0; i < args.length; i++) {
            String value = args[i];
            if(value.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    public void addAdditionalIDLPath(String path) {
        try {
            String p = new File(path).getCanonicalPath();
            idlPath.add(p);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addAdditionalIDLRefPath(String path) {
        try {
            String p = new File(path).getCanonicalPath();
            idlPathRef.add(p);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
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

    public String[] getIDLPath() {
        String [] path = new String[idlPath.size()];
        idlPath.toArray(path);
        return path;
    }

    public String[] getIDLRefPath() {
        String [] path = new String[idlPathRef.size()];
        idlPathRef.toArray(path);
        return path;
    }

    public void addAdditionalSourceDirs(String path) {
        additionalSourceDirs.add(path);
    }

    public String[] getAdditionalSourceDirs() {
        String [] path = new String[additionalSourceDirs.size()];
        additionalSourceDirs.toArray(path);
        return path;
    }

    public String getModuleBaseJavaDir() {
        return moduleBaseJavaDir;
    }

    public String getSourceDir() {
        return sourcePath;
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
}