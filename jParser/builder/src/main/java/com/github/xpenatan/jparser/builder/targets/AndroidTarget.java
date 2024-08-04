package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;
import com.github.xpenatan.jparser.builder.JProcess;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor.FileType;
import java.util.ArrayList;

public class AndroidTarget extends DefaultBuildTarget {

    public String androidABIS = "all";
    public String androidPlatform = "android-19";

    public ArrayList<String> customArgs = new ArrayList<>();

    public AndroidTarget() {
        this.libDirSuffix = "android/";
        this.tempBuildDir = "target/android";

        cppFlags.add("-O2");
        cppFlags.add("-Wall");
        cppFlags.add("-D__ANDROID__");
        cppFlags.add("-fvisibility=hidden");
        cppFlags.add("-std=c++17");
        linkerFlags.add("-lm");

        cppInclude.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/linux");
    }

    @Override
    protected boolean build(BuildConfig config, CustomFileDescriptor childTarget) {
        CustomFileDescriptor androidDir = config.buildDir;
        if(!androidDir.exists()) {
            androidDir.mkdirs();
        }

        CustomFileDescriptor applicationTemplate = new CustomFileDescriptor("android/Application.mk", FileType.Classpath);
        String applicationStr = applicationTemplate.readString();
        applicationStr = applicationStr.replace("%androidABIs%", androidABIS);
        applicationStr = applicationStr.replace("%androidPlat%", androidPlatform);
        CustomFileDescriptor applicationFile = androidDir.child(applicationTemplate.name());
        applicationFile.writeString(applicationStr, false);

        CustomFileDescriptor androidTemplate = new CustomFileDescriptor("android/Android.mk", FileType.Classpath);
        String androidStr = androidTemplate.readString();

        String headerDirsStr = "";
        for(String headerDir : headerDirs) {
            headerDir = headerDir.replace("-I", "");
            headerDirsStr += headerDir + " ";
        }
        headerDirsStr = headerDirsStr.trim();

        String cppFlagsStr = "";

        for(String cppFlag : cppFlags) {
            cppFlagsStr += cppFlag + " ";
        }
        cppFlagsStr = cppFlagsStr.trim();

        String linkerFlagsStr = "";

        for(String linkerFlag : linkerFlags) {
            linkerFlagsStr += linkerFlag + " ";
        }
        linkerFlagsStr = linkerFlagsStr.trim();

        ArrayList<CustomFileDescriptor> cppFiles = new ArrayList<>(getCPPFiles(config.sourceDir, cppInclude, cppExclude, filterCPPSuffix));
        for(CustomFileDescriptor sourceDir : config.additionalSourceDirs) {
            ArrayList<CustomFileDescriptor> cppFiles1 = getCPPFiles(sourceDir, cppInclude, cppExclude, filterCPPSuffix);
            cppFiles.addAll(cppFiles1);
        }

        String srcFilesStr = "";
        for(CustomFileDescriptor file : cppFiles) {
            String path = file.path();
            String sourceBasePath = config.buildDir.path();
            String pathWithoutBase = path.replace(sourceBasePath, "");
            pathWithoutBase = pathWithoutBase.replaceFirst("/", "");
            srcFilesStr += "FILE_LIST += $(wildcard $(LOCAL_PATH)/" + pathWithoutBase + ")\n";
        }

        srcFilesStr = srcFilesStr.trim();

        androidStr = androidStr.replace("%libName%", config.libName);
        androidStr = androidStr.replace("%headerDirs%", headerDirsStr);
        androidStr = androidStr.replace("%cppFlags%", cppFlagsStr);
        androidStr = androidStr.replace("%linkerFlags%", linkerFlagsStr);
        androidStr = androidStr.replace("%srcFiles%", srcFilesStr);

        CustomFileDescriptor androidFile = androidDir.child(androidTemplate.name());
        androidFile.writeString(androidStr, false);

        String ndkHome = System.getenv("NDK_HOME");

        if(ndkHome != null) {
            ndkHome += "/";
        }
        else {
            ndkHome = "";
        }

        String androidCommand = ndkHome + "ndk-build";
        if(isWindows()) {
            androidCommand += ".cmd";
        }

        if(multiCoreCompile) {
            int i = Runtime.getRuntime().availableProcessors();
            customArgs.add("-j" + i );
        }

        CustomFileDescriptor libTarget = config.libDir.child("android");
        ArrayList<String> commands = new ArrayList<>();
        commands.add(androidCommand);
        commands.addAll(customArgs);
        commands.add("NDK_PROJECT_PATH=.");
        commands.add("NDK_APPLICATION_MK=Application.mk");
        commands.add(" NDK_LIBS_OUT=" + libTarget.path());
        if(!JProcess.startProcess(androidDir.file(), commands)) {
            return false;
        }
        return true;
    }
}
