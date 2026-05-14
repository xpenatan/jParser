package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLFile;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BuildToolOptions {
    private String modulePath;
    public final String libName;
    public final String webModuleName;
    public final String packageName;
    public boolean generateWeb = false;
    public boolean generateJNI = false;
    public boolean generateFFM = false;
    public boolean generateCore = true;
    /** Keep parsed command comments like [-FFM;-REPLACE_BLOCK] in generated Java output. */
    public boolean keepGeneratedCommandComments = false;

    /** Optional build-time FFM critical policy used by FFMCodeParser generation. */
    public final FFMClassData ffmClassData = new FFMClassData();

    /** Optional build-time JNI naming policy used by CppCodeParser generation. */
    public final JNIClassData jniClassData = new JNIClassData();

    /** Name of the idl file located in [Module Build Path] + src/main/cpp/myidl.idl. The default is libName but can be changed. */
    public String idlName;

    private String modulePrefix;
    private String moduleBaseSuffix;
    private String moduleBuildSuffix;
    private String moduleCoreSuffix;
    private String moduleJNISuffix;
    private String moduleWebSuffix;
    private String moduleFFMSuffix;
    private String moduleBasePath;
    private String moduleBuildPath;
    private String moduleBuildCPPPath;
    private String moduleCorePath;
    private String moduleJNIPath;
    private String moduleTeavmPath;
    private String moduleFFMPath;
    private ArrayList<IDLFile> idlPath = new ArrayList<>();
    private ArrayList<IDLFile> idlPathRef = new ArrayList<>();
    private ArrayList<String> additionalSourceDirs = new ArrayList<>();
    private String moduleBaseJavaDir;
    private String sourcePath;
    private String customSourceDir;
    private String libsDir;
    private String cppDestinationPath;
    private String cppPath;
    private String[] args;

    public BuildToolOptions(BuildToolParams params, String ... args) {
        this.libName = params.libName;
        this.idlName = params.idlName;
        this.webModuleName = params.webModuleName;
        this.packageName = params.packageName;
        this.modulePrefix = params.modulePrefix;
        this.moduleBaseSuffix = params.moduleBaseSuffix;
        this.moduleBuildSuffix = params.moduleBuildSuffix;
        this.moduleCoreSuffix = params.moduleCoreSuffix;
        this.moduleJNISuffix = params.moduleJNISuffix;
        this.moduleWebSuffix = params.moduleWebSuffix;
        this.moduleFFMSuffix = params.moduleFFMSuffix;
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
        moduleBasePath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleBaseSuffix, "-base");
        moduleBuildPath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleBuildSuffix, "-build");
        moduleCorePath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleCoreSuffix, "-core");
        moduleJNIPath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleJNISuffix, "-jni");
        moduleTeavmPath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleWebSuffix, "-web");
        moduleFFMPath = modulePath + "/" + modulePrefix + resolveModuleSuffix(moduleFFMSuffix, "-ffm");

        moduleBaseJavaDir = moduleBasePath + "/src/main/java";
        cppPath = moduleBuildPath + "/src/main/cpp/";

        if(idlName != null) {
            String idlPathItem = cppPath + idlName + ".idl";
            idlPath.add(IDLReader.parseFile(idlPathItem));
        }

        customSourceDir = cppPath + "custom/";

        moduleBuildCPPPath = moduleBuildPath + "/build/c++";
        libsDir = moduleBuildCPPPath + "/libs";
        cppDestinationPath = moduleBuildCPPPath + "/src";
    }

    private static String resolveModuleSuffix(String moduleSuffix, String defaultSuffix) {
        if(moduleSuffix == null) {
            return defaultSuffix;
        }
        String normalized = moduleSuffix.trim();
        if(normalized.isEmpty()) {
            return defaultSuffix;
        }
        if(normalized.startsWith("-")) {
            return normalized;
        }
        return "-" + normalized;
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

    public void addAdditionalIDLPath(IDLFile idlFile) {
        idlPath.add(idlFile);
    }

    public void addAdditionalIDLRefPath(IDLFile idlFile) {
        idlPathRef.add(idlFile);
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

    public String getModuleJNIPath() {
        return moduleJNIPath;
    }

    public String getJNIJavaOutputPath() {
        return moduleJNIPath + "/src/main/java";
    }

    public String getFFMJavaOutputPath() {
        return moduleFFMPath + "/src/main/java";
    }

    public String getTeaVMJavaOutputPath() {
        return moduleTeavmPath + "/src/main/java";
    }


    public String getModuleTeaVMPath() {
        return moduleTeavmPath;
    }

    public String getModuleFFMPath() {
        return moduleFFMPath;
    }

    public IDLFile[] getIDL() {
        IDLFile [] path = new IDLFile[idlPath.size()];
        idlPath.toArray(path);
        return path;
    }

    public IDLFile[] getIDLRef() {
        IDLFile [] path = new IDLFile[idlPathRef.size()];
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

    public String getModulePath() {
        return modulePath;
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
         * Module prefix name. ex: imgui. So it will be imgui-core, imgui-web, etc.
         */
        public String modulePrefix;

        /**
         * Optional module suffix used for the base module. Defaults to "-base".
         */
        public String moduleBaseSuffix;

        /**
         * Optional module suffix used for the build module. Defaults to "-build".
         */
        public String moduleBuildSuffix;

        /**
         * Optional module suffix used for the core module. Defaults to "-core".
         */
        public String moduleCoreSuffix;

        /**
         * Optional module suffix used for the JNI module. Defaults to "-jni".
         */
        public String moduleJNISuffix;

        /**
         * Optional module suffix used for the web module. Defaults to "-web".
         */
        public String moduleWebSuffix;

        /**
         * Optional module suffix used for the FFM module. Defaults to "-ffm".
         */
        public String moduleFFMSuffix;

        /**
         * The C++ source path or relative path specifies the location where the build is executed, such as "/build/MyCPlusPlusLib".
         */
        public String cppSourcePath;

        /**
         * The full parent path that contains all the required modules. (Lib-core, Lib-web, Lib-jni, Lib-ffm, etc.)<br>
         * If not specified, it will be the current parent directory of the build instance.
         */
        public String modulePath;
    }
}
