package com.github.xpenatan.jParser.builder;

import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class BuildConfig {
    public final CustomFileDescriptor buildRootPath;
    public final CustomFileDescriptor buildRootGenSourcePath;
    public final ArrayList<CustomFileDescriptor> additionalSourceDirs = new ArrayList<>();
    public final CustomFileDescriptor compiledLibsPath;
    public final String libName;

    public BuildConfig(String libName, String buildRootPath, String buildRootGenSourcePath, String compiledLibsPath) {
        this.buildRootPath = new CustomFileDescriptor(buildRootPath);
        this.buildRootGenSourcePath = new CustomFileDescriptor(buildRootGenSourcePath);
        this.compiledLibsPath = new CustomFileDescriptor(compiledLibsPath);
        this.libName = libName;
    }

    public BuildConfig(BuildToolOptions op) {
        String buildRootDir = op.getModuleBuildCPPPath();
        String buildRootGenSourcePath = op.getCPPDestinationPath();
        String compiledLibsPath = op.getLibsDir();
        String libName = op.libName;
        String sourcePath = op.getSourceDir();

        this.buildRootPath = new CustomFileDescriptor(buildRootDir);
        this.buildRootGenSourcePath = new CustomFileDescriptor(buildRootGenSourcePath);
        if(sourcePath != null) {
            additionalSourceDirs.add(new CustomFileDescriptor(sourcePath));
        }
        String customSourceDir = op.getCustomSourceDir();
        if(customSourceDir != null) {
            additionalSourceDirs.add(new CustomFileDescriptor(customSourceDir));
        }

        String[] sourcePaths = op.getAdditionalSourceDirs();
        for(int i = 0; i < sourcePaths.length; i++) {
            String path = sourcePaths[i];
            additionalSourceDirs.add(new CustomFileDescriptor(path));
        }

        this.compiledLibsPath = new CustomFileDescriptor(compiledLibsPath);

        this.libName = libName;

        copyJniHeaders();
    }

    public void copyJniHeaders() {
        final String pack = "headers";
        String files[] = {"classfile_constants.h", "jawt.h", "jdwpTransport.h", "jni.h", "linux/jawt_md.h", "linux/jni_md.h",
                "mac/jni_md.h", "win32/jawt_md.h", "win32/jni_md.h"};

        for (String file : files) {
            CustomFileDescriptor child = buildRootPath.child("jni-headers").child(file);
            new CustomFileDescriptor(pack, CustomFileDescriptor.FileType.Classpath).child(file).copyTo(child, true);
        }
    }
}
