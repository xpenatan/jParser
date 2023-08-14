package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildTarget;

public class AndroidTarget extends BuildTarget {
    public AndroidTarget() {
        this.tempBuildDir = "target/android";

        cFlags = "-O2 -Wall -D__ANDROID__";
        cppFlags = "-O2 -Wall -D__ANDROID__";
    }

    @Override
    protected void setup() {

    }
}
