package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;

public class EmscriptenLibTarget extends BuildTarget {

    String EMSCRIPTEN_ROOT = System.getenv("EMSDK") + "/upstream/emscripten/";

    public EmscriptenLibTarget() {
        this.libDirSuffix = "windows/";
        this.tempBuildDir = "target/windows";

        cppCompiler.clear();
        linkerCompiler.clear();

        String cppCompilerr = EMSCRIPTEN_ROOT + "em++";
        String cppLinkeer = EMSCRIPTEN_ROOT + "emar";
        if(isWindows()) {
            cppCompilerr += ".bat";
            cppLinkeer += ".bat";
        }
        cppCompiler.add(cppCompilerr);
        linkerCompiler.add(cppLinkeer);

        cppFlags.add("-std=c++17");
        cppFlags.add("-c");
        libSuffix = "64.dll";
    }

    @Override
    protected void setup(BuildConfig config) {
    }
}
