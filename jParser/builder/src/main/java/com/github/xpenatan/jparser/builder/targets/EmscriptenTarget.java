package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.builder.JProcess;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.util.ArrayList;

public class EmscriptenTarget extends BuildTarget {

    private CustomFileDescriptor idlFile;

    String EMSCRIPTEN_ROOT = System.getenv("EMSDK") + "upstream/emscripten/";
    String WEBIDL_BINDER_SCRIPT = EMSCRIPTEN_ROOT + "tools/webidl_binder.py";

    public EmscriptenTarget(String idlFile) {
        this.tempBuildDir = "target/emscripten";
        this.idlFile = new CustomFileDescriptor(idlFile);
        if(!this.idlFile.exists()) {
            throw new RuntimeException("IDL file does not exist: " + idlFile);
        }

        long initialMemory = 64 * 1024 * 1024;

        cppCompiler = EMSCRIPTEN_ROOT + "em++";
        if(isWindows()) {
            cppCompiler += ".bat";
        }

        linkerFlags.add("-O3");
        linkerFlags.add("-std=c++14");
        linkerFlags.add("--llvm-lto 1");
        linkerFlags.add("-s ALLOW_MEMORY_GROWTH=1");
        linkerFlags.add("-s ALLOW_TABLE_GROWTH=1");
        linkerFlags.add("-s MODULARIZE=1");
        linkerFlags.add("-s NO_FILESYSTEM=1");
        linkerFlags.add("-s INITIAL_MEMORY=" + initialMemory);
        linkerFlags.add("-s EXPORTED_FUNCTIONS=['_free','_malloc']");
        linkerFlags.add("-s EXPORTED_RUNTIME_METHODS=['UTF8ToString']");
        linkerFlags.add("-s WASM=1");
        linkerFlags.add("-s SINGLE_FILE=1");

        libSuffix = ".wasm.js";

        cppIncludes.add("**/jsglue/*.cpp");
    }

    @Override
    protected void setup(BuildConfig config, ArrayList<CustomFileDescriptor> cppFiles) {


    }

    @Override
    protected boolean build(BuildConfig config) {
        CustomFileDescriptor childTarget = config.buildDir.child(tempBuildDir);
        if(childTarget.exists()) {
            childTarget.delete();
        }
        childTarget.mkdirs();

        CustomFileDescriptor jsglueDir = config.sourceDir.child("jsglue");
        if(!jsglueDir.exists()) {
            jsglueDir.mkdirs();
        }

        config.emscriptenCustomCodeDir.copyTo(jsglueDir, false);

        CustomFileDescriptor mergedIDLFile = mergeIDLFile(jsglueDir);

        CustomFileDescriptor idlHelperCPP = new CustomFileDescriptor("IDLHelper.h", CustomFileDescriptor.FileType.Classpath);
        idlHelperCPP.copyTo(jsglueDir, false);

        CustomFileDescriptor cppFile = jsglueDir.child(idlHelperCPP.name());
        headerDirs.add("-include" + cppFile.path());

        String jsGluePath = jsglueDir.path() + File.separator;

        CustomFileDescriptor postFile = new CustomFileDescriptor("emscripten/post.js", CustomFileDescriptor.FileType.Classpath);
        String s = postFile.readString();
        s = s.replace("[MODULE_NAME]", config.libName);

        CustomFileDescriptor postJS = new CustomFileDescriptor(jsGluePath + "post.js");
        postJS.writeString(s, false);
        String postPath = postJS.path();

        linkerFlags.add("--post-js " + jsGluePath + "glue.js");
        linkerFlags.add("--extern-post-js " + postPath);
        linkerFlags.add("-s EXPORT_NAME='" + config.libName + "'");

        String generateGlueCommand = "python " + WEBIDL_BINDER_SCRIPT + " " + mergedIDLFile + " glue";
        if(!JProcess.startProcess(jsglueDir.file(), generateGlueCommand)) {
            return false;
        }

        cppFlags.add("-c");

        return super.build(config);
    }

    private CustomFileDescriptor mergeIDLFile(CustomFileDescriptor jsglueDir) {
        String idlStr = idlFile.readString();
        CustomFileDescriptor idlHelper = new CustomFileDescriptor("IDLHelper.idl", CustomFileDescriptor.FileType.Classpath);
        String idlHelperStr = idlHelper.readString();
        String mergedIdlStr = idlStr + "\n\n" + idlHelperStr;
        CustomFileDescriptor mergedIdlFile = jsglueDir.child(idlFile.name());
        mergedIdlFile.writeString(mergedIdlStr, false);
        return mergedIdlFile;
    }
}
