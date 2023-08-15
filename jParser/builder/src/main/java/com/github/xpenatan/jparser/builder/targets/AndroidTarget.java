package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class AndroidTarget extends BuildTarget {
    public AndroidTarget() {
        this.tempBuildDir = "target/android";

        cFlags.add("-O2 -Wall -D__ANDROID__");
        cppFlags.add("-O2 -Wall -D__ANDROID__");
    }
}
