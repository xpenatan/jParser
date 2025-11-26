package com.github.xpenatan.jParser.builder;

import com.github.xpenatan.jParser.builder.tool.BuildToolOptions;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;

public class BuildConfig {
    public final CustomFileDescriptor buildDir;
    public final CustomFileDescriptor buildSourceDir;
    public final ArrayList<CustomFileDescriptor> additionalSourceDirs = new ArrayList<>();
    public final CustomFileDescriptor libDir;
    public final String libName;
    private final BuildToolOptions op;

    public BuildConfig(BuildToolOptions op) {
        this.op = op;
        String buildSourceDir = op.getCPPDestinationPath();
        String buildDir = op.getModuleBuildCPPPath();
        String libsDir = op.getLibsDir();
        String libName = op.libName;
        String sourcePath = op.getSourceDir();

        this.buildDir = new CustomFileDescriptor(buildDir);
        this.buildSourceDir = new CustomFileDescriptor(buildSourceDir);
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

        this.libDir = new CustomFileDescriptor(libsDir);

        this.libName = libName;

        copyJniHeaders(this.buildDir);
    }

    protected void copyJniHeaders (CustomFileDescriptor buildDir) {
        final String pack = "headers";
        String files[] = {"classfile_constants.h", "jawt.h", "jdwpTransport.h", "jni.h", "linux/jawt_md.h", "linux/jni_md.h",
                "mac/jni_md.h", "win32/jawt_md.h", "win32/jni_md.h"};

        for (String file : files) {
            CustomFileDescriptor child = buildDir.child("jni-headers").child(file);
            new CustomFileDescriptor(pack, CustomFileDescriptor.FileType.Classpath).child(file).copyTo(child, true);
        }
    }
}
