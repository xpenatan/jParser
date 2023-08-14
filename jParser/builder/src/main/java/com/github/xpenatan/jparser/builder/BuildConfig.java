package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;

public class BuildConfig {
    public CustomFileDescriptor buildDir;
    public CustomFileDescriptor sourceDir;
    public CustomFileDescriptor libsDir;
    public String libName;

    public BuildConfig(String sourceDir, String buildDir, String libsDir, String libName) {
        this.sourceDir = new CustomFileDescriptor(sourceDir);
        this.buildDir = new CustomFileDescriptor(buildDir);
        this.libsDir = new CustomFileDescriptor(libsDir);
        this.libName = libName;
    }
}
