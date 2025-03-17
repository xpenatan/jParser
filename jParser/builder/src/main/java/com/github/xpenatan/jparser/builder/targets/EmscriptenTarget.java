package com.github.xpenatan.jparser.builder.targets;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.DefaultBuildTarget;
import com.github.xpenatan.jparser.builder.JProcess;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;

public class EmscriptenTarget extends DefaultBuildTarget {

    public static boolean SKIP_GLUE_CODE = false;
    public final static String EMSCRIPTEN_ROOT = (System.getenv("EMSDK") + "/upstream/emscripten/").replace("\\", "/").replace("//", "/");

    public IDLReader idlReader;

    public static boolean DEBUG_BUILD = false;
    public static boolean IS_WASM = true;

    public boolean isStatic = false;
    public boolean compileGlueCode = true;

    String WEBIDL_BINDER_SCRIPT = EMSCRIPTEN_ROOT + "tools/webidl_binder.py";

    public long initialMemory = 64 * 1024 * 1024;
    public long stackSize = 1048576;

    public EmscriptenTarget() {
        this(null);
    }

    public EmscriptenTarget(IDLReader idlReader) {
        this.libDirSuffix = "emscripten/";
        this.tempBuildDir = "target/emscripten";
        this.idlReader = idlReader;

        cppCompiler.clear();
        linkerCompiler.clear();

        String cppCompilerr = EMSCRIPTEN_ROOT + "em++";
        if(isWindows()) {
            cppCompilerr += ".bat";
        }
        cppCompiler.add(cppCompilerr);
        linkerCompiler.add(cppCompilerr);

        if(IS_WASM) {
            libSuffix = ".wasm.js";
        }
        else {
            libSuffix = ".js";
        }

        cppFlags.add("-c");
        cppFlags.add("-std=c++17");

        if(DEBUG_BUILD) {
            cppFlags.add("-O0");
            cppFlags.add("-g2");
        }
        else {
            cppFlags.add("-O3");
        }
    }

    @Override
    protected boolean build(BuildConfig config, CustomFileDescriptor buildTargetTemp) {
        String libName = this.libName;
        if(libName.isEmpty()) {
            libName = config.libName;
        }

        CustomFileDescriptor jsglueDir = config.buildSourceDir.child("jsglue");
        if(!jsglueDir.exists()) {
            jsglueDir.mkdirs();
        }

        if(compileGlueCode && !isStatic) {
            cppInclude.add("**/jsglue/*.cpp");
            headerDirs.add("-include" + idlHelperHFile.path());
        }

        if(idlReader != null) {
            CustomFileDescriptor mergedIDLFile = mergeIDLFile(jsglueDir);
            if(!SKIP_GLUE_CODE && !createGlueCode(mergedIDLFile, jsglueDir)) {
                return false;
            }
        }

        if(isStatic) {
            linkerCompiler.clear();

            String cppCompilerr = EMSCRIPTEN_ROOT + "emar";
            if(isWindows()) {
                cppCompilerr += ".bat";
            }

            linkerCompiler.add(cppCompilerr);
            linkerFlags.add("rcs");
            libSuffix = "_.a";
        }
        else {
            String postPath = createPostJS(jsglueDir, libName);
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
            linkerFlags.add("STACK_SIZE=" + stackSize);
            linkerFlags.add("-s");
            linkerFlags.add("EXPORTED_FUNCTIONS=['_free','_malloc']");
            linkerFlags.add("-s");
            linkerFlags.add("EXPORTED_RUNTIME_METHODS=['UTF8ToString']");
            if(DEBUG_BUILD) {
                linkerFlags.add("-s");
                linkerFlags.add("ASSERTIONS=1");
                linkerFlags.add("-s");
                linkerFlags.add("SAFE_HEAP=1");
            }
            if(IS_WASM) {
                linkerFlags.add("-s");
                linkerFlags.add("WASM=1");
                linkerFlags.add("-s");

                // Disable big int because of conversion bug
                linkerFlags.add("WASM_BIGINT=0");
            }
            else {
                linkerFlags.add("-s");
                linkerFlags.add("WASM=0");
            }
            linkerFlags.add("-s");
            linkerFlags.add("SINGLE_FILE=1");

            linkerFlags.add("--post-js");
            linkerFlags.add(jsglueDir.path() + "/glue.js");
            linkerFlags.add("--extern-post-js");
            linkerFlags.add(postPath);
            linkerFlags.add("-s");
            linkerFlags.add("EXPORT_NAME='" + libName + "'");
        }

        return super.build(config, buildTargetTemp);
    }

    @Override
    protected void onLink(ArrayList<CustomFileDescriptor> compiledObjects, String objFilePath, String libPath) {
        if(isStatic) {
            linkerCommands.addAll(linkerCompiler);
            linkerCommands.addAll(linkerFlags);
            linkerCommands.add(libPath);
            linkerCommands.add("@" + objFilePath);
        }
        else {
            super.onLink(compiledObjects, objFilePath, libPath);
        }
    }

    private String createPostJS(CustomFileDescriptor jsglueDir, String libName) {
        CustomFileDescriptor postFile = new CustomFileDescriptor("emscripten/post.js", CustomFileDescriptor.FileType.Classpath);
        String s = postFile.readString();
        s = s.replace("[MODULE_NAME]", libName);
        CustomFileDescriptor postJS = new CustomFileDescriptor(jsglueDir + "/post.js");
        postJS.writeString(s, false);
        return postJS.path();
    }

    private boolean createGlueCode(CustomFileDescriptor mergedIDLFile, CustomFileDescriptor jsglueDir) {
        String pythonCmd = "python";
        if(isUnix()) {
            pythonCmd = "python3";
        }

        ArrayList<String> generateGlueCommand = new ArrayList<>();
        generateGlueCommand.add(pythonCmd);
        generateGlueCommand.add(WEBIDL_BINDER_SCRIPT);
        generateGlueCommand.add(mergedIDLFile.toString());
        generateGlueCommand.add("glue");
        return JProcess.startProcess(jsglueDir.file(), generateGlueCommand);
    }

    private CustomFileDescriptor mergeIDLFile(CustomFileDescriptor jsglueDir) {
        String idlStr = idlReader.mergeIDLFiles();
        CustomFileDescriptor mergedIdlFile = jsglueDir.child("IDLMerged.idl");
        mergedIdlFile.writeString(idlStr, false);
        return mergedIdlFile;
    }
}
