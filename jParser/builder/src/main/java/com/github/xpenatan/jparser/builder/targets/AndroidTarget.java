package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.builder.JProcess;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor.FileType;
import java.util.ArrayList;

public class AndroidTarget extends BuildTarget {

    public String androidABIS = "all";
    public String androidPlatform = "android-19";

    public AndroidTarget() {
        this.tempBuildDir = "target/android";

        cFlags.add("-O2 -Wall -D__ANDROID__");
        cppFlags.add("-O2 -Wall -D__ANDROID__");
        linkerFlags.add("-lm");

        cppIncludes.add("**/jniglue/JNIGlue.cpp");
        headerDirs.add("jni-headers/");
        headerDirs.add("jni-headers/linux");
    }

    @Override
    protected boolean build(BuildConfig config) {
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

        ArrayList<CustomFileDescriptor> cppFiles = getCPPFiles(config.sourceDir, cppIncludes);

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

        String androidCommand = ndkHome + "/ndk-build";
        if(isWindows()) {
            androidCommand += ".cmd";
        }

        String command = androidCommand;
        command += " NDK_PROJECT_PATH=. NDK_APPLICATION_MK=Application.mk";
        if(!JProcess.startProcess(androidDir.file(), command)) {
            return false;
        }
        return true;
    }
}
