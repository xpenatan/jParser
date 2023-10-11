package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;

public class BuildConfig {
    public CustomFileDescriptor buildDir;
    public CustomFileDescriptor sourceDir;
    public CustomFileDescriptor libDir;
    public String libName;

    public BuildConfig(String sourceDir, String buildDir, String libsDir, String libName) {
        this.sourceDir = new CustomFileDescriptor(sourceDir);
        this.buildDir = new CustomFileDescriptor(buildDir);
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
