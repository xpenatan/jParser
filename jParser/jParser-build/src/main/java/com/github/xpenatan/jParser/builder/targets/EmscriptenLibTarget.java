package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.io.File;

// Test Target
@Deprecated
public class EmscriptenLibTarget extends DefaultBuildTarget {

    String EMSCRIPTEN_ROOT = System.getenv("EMSDK") + "/upstream/emscripten/";

    public EmscriptenLibTarget() {
        this.libDirSuffix = "emscripten/";
        this.tempBuildDir = "target/emscripten";

        long initialMemory = 64 * 1024 * 1024;

        cppCompiler.clear();
        linkerCompiler.clear();

        String cppCompilerr = EMSCRIPTEN_ROOT + "em++";
        if(isWindows()) {
            cppCompilerr += ".bat";
        }
        cppCompiler.add(cppCompilerr);
        linkerCompiler.add(cppCompilerr);

        linkerFlags.add("-O3");
        linkerFlags.add("--llvm-lto");
        linkerFlags.add("1");
        linkerFlags.add("-s");
        linkerFlags.add("ALLOW_MEMORY_GROWTH=1");
        linkerFlags.add("-s");
        linkerFlags.add("ALLOW_TABLE_GROWTH=1");
        linkerFlags.add("-s");
        linkerFlags.add("MODULARIZE=1");
        linkerFlags.add("-s");
        linkerFlags.add("NO_FILESYSTEM=0");
        linkerFlags.add("-s");
        linkerFlags.add("INITIAL_MEMORY=" + initialMemory);
        linkerFlags.add("-s");
        linkerFlags.add("EXPORTED_FUNCTIONS=['_free','_malloc']");
        linkerFlags.add("-s");
        linkerFlags.add("EXPORTED_RUNTIME_METHODS=['UTF8ToString']");
        linkerFlags.add("-s");
        linkerFlags.add("WASM=1");
        linkerFlags.add("-s");
        linkerFlags.add("SINGLE_FILE=1");

        libSuffix = ".wasm.js";
    }

    @Override
    protected boolean build(BuildConfig config, CustomFileDescriptor buildTargetTemp) {
        CustomFileDescriptor jsglueDir = config.buildSourceDir.child("jsglue");
        if(!jsglueDir.exists()) {
            jsglueDir.mkdirs();
        }

        String jsGluePath = jsglueDir.path() + File.separator;

        CustomFileDescriptor postFile = new CustomFileDescriptor("emscripten/post.js", CustomFileDescriptor.FileType.Classpath);
        String s = postFile.readString();

        String libName = this.libName;
        if(libName.isEmpty()) {
            libName = config.libName;
        }

        s = s.replace("[MODULE_NAME]", libName);

        CustomFileDescriptor postJS = new CustomFileDescriptor(jsGluePath + "post.js");
        postJS.writeString(s, false);
        String postPath = postJS.path();

        linkerFlags.add("--extern-post-js");
        linkerFlags.add(postPath);
        linkerFlags.add("-s");
        linkerFlags.add("EXPORT_NAME='" + libName + "'");
        cppFlags.add("-c");
        return super.build(config, buildTargetTemp);
    }
}
