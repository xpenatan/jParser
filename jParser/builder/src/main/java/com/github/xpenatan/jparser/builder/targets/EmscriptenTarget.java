package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildTarget;
import com.github.xpenatan.jparser.builder.JProcess;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.util.ArrayList;

public class EmscriptenTarget extends BuildTarget {

    private CustomFileDescriptor idlFile;

    String EMSCRIPTEN_ROOT = System.getenv("EMSDK") + "/upstream/emscripten/";
    String WEBIDL_BINDER_SCRIPT = EMSCRIPTEN_ROOT + "tools/webidl_binder.py";

    public EmscriptenTarget(String idlFile) {
        this.libDirSuffix = "emscripten/";
        this.tempBuildDir = "target/emscripten";
        this.idlFile = new CustomFileDescriptor(idlFile);

        if(!this.idlFile.exists()) {
            throw new RuntimeException("IDL file does not exist: " + idlFile);
        }

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
        linkerFlags.add("-std=c++17");
        linkerFlags.add("--llvm-lto");
        linkerFlags.add("1");
        linkerFlags.add("-s");
        linkerFlags.add("ALLOW_MEMORY_GROWTH=1");
        linkerFlags.add("-s");
        linkerFlags.add("ALLOW_TABLE_GROWTH=1");
        linkerFlags.add("-s");
        linkerFlags.add("MODULARIZE=1");
        linkerFlags.add("-s");
        linkerFlags.add("NO_FILESYSTEM=1");
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

        cppIncludes.add("**/jsglue/*.cpp");
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

        CustomFileDescriptor mergedIDLFile = mergeIDLFile(jsglueDir);

        CustomFileDescriptor idlHelperCPP = new CustomFileDescriptor("IDLHelper.h", CustomFileDescriptor.FileType.Classpath);
        idlHelperCPP.copyTo(jsglueDir, false);

        CustomFileDescriptor cppFile = jsglueDir.child(idlHelperCPP.name());
        headerDirs.add("-include" + cppFile.path());

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

        linkerFlags.add("--post-js");
        linkerFlags.add(jsGluePath + "glue.js");
        linkerFlags.add("--extern-post-js");
        linkerFlags.add(postPath);
        linkerFlags.add("-s");
        linkerFlags.add("EXPORT_NAME='" + libName + "'");

        String pythonCmd = "python";
        if(isUnix()) {
            pythonCmd = "python3";
        }

        ArrayList<String> generateGlueCommand = new ArrayList<>();
        generateGlueCommand.add(pythonCmd);
        generateGlueCommand.add(WEBIDL_BINDER_SCRIPT);
        generateGlueCommand.add(mergedIDLFile.toString());
        generateGlueCommand.add("glue");
        if(!JProcess.startProcess(jsglueDir.file(), generateGlueCommand)) {
            return false;
        }

        cppFlags.add("-c");

        return super.build(config);
    }

    private CustomFileDescriptor mergeIDLFile(CustomFileDescriptor jsglueDir) {
        String idlStr = idlFile.readString();

        if(!JParser.CREATE_IDL_HELPER) {
            return idlFile;
        }

        CustomFileDescriptor idlHelper = new CustomFileDescriptor("IDLHelper.idl", CustomFileDescriptor.FileType.Classpath);
        String idlHelperStr = idlHelper.readString();
        String mergedIdlStr = idlStr + "\n\n" + idlHelperStr;
        CustomFileDescriptor mergedIdlFile = jsglueDir.child(idlFile.name());
        mergedIdlFile.writeString(mergedIdlStr, false);
        return mergedIdlFile;
    }
}
