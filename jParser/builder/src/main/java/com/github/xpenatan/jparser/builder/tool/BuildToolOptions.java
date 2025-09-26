package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BuildToolOptions {
    private String modulePath;
    public final String libName;
    public final String webModuleName;
    public final String packageName;
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
    private String cppPath;
    private String[] args;

//    public BuildToolOptions(String libName, String libBasePackage, String modulePrefix, String cppSourcePath, String ... args) {
//        this(libName, libBasePackage, modulePrefix, cppSourcePath, null, args);
//    }
//
//    /**
//     *
//     * @param libName module name
//     * @param libBasePackage module package that all classes will be in
//     * @param modulePrefix module prefix name. ex: imgui. So it will be imgui-core, imgui-teavm, etc.
//     * @param cppSourcePath full path where the source is located
//     * @param libPath root path
//     * @param args windows64, linux64, mac64, mac64arm, android, ios, teavm
//     */
//    public BuildToolOptions(String libName, String libBasePackage, String modulePrefix, String cppSourcePath, String libPath, String ... args) {
//    }

    public BuildToolOptions(BuildToolParams params, String ... args) {
        this.libName = params.libName;
        this.idlName = params.idlName;
        this.webModuleName = params.webModuleName;
        this.packageName = params.packageName;
        this.modulePrefix = params.modulePrefix;
        this.modulePath = params.modulePath;
        this.args = args;

        if(params.cppSourcePath != null) {
            boolean exists = new CustomFileDescriptor(params.cppSourcePath).exists();
            if(exists) {
                this.sourcePath = params.cppSourcePath.replace("\\", "/");
            }
            else {
                try {
                    this.sourcePath = new File(".", params.cppSourcePath).getCanonicalPath().replace("\\", "/");
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(!this.sourcePath.endsWith("/")) {
                this.sourcePath += "/";
            }
        }
        setup();
    }

    private void setup() {
        if(modulePath == null) {
            try {
                modulePath = new File("./../").getCanonicalPath().replace("\\", "/");
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        moduleBasePath = modulePath + "/" + modulePrefix + "-base";
        moduleBuildPath = modulePath + "/" + modulePrefix + "-build";
        moduleCorePath = modulePath + "/" + modulePrefix + "-core";
        moduleTeavmPath = modulePath + "/" + modulePrefix + "-teavm";

        moduleBaseJavaDir = moduleBasePath + "/src/main/java";
        cppPath = moduleBuildPath + "/src/main/cpp/";
        String idlPathItem = cppPath + idlName + ".idl";
        idlPath.add(idlPathItem);
        customSourceDir = cppPath + "custom/";

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

    public String getCPPPath() {
        return cppPath;
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

    public static class BuildToolParams {
        /**
         * Name of the native file that will be generated.
         */
        public String libName;

        /**
         * Name of the idl file located in [Module Build Path] + src/main/cpp/myidl.idl.
         */
        public String idlName;

        /**
         * Emscripten module name that will use in the generated classes to call Javascript code. It can be the same as libName. <br>
         */
        public String webModuleName;

        /**
         * The package name that the generated classes will use.
         */
        public String packageName;

        /**
         * Module prefix name. ex: imgui. So it will be imgui-core, imgui-teavm, etc.
         */
        public String modulePrefix;

        /**
         * The C++ source path or relative path specifies the location where the build is executed, such as "/build/MyCPlusPlusLib".
         */
        public String cppSourcePath;

        /**
         * The full parent path that contains all the required modules. (Lib-core, Lib-teavm, Lib-desktop, etc.)<br>
         * If not specified, it will be the current parent directory of the build instance.
         */
        public String modulePath;
    }
}