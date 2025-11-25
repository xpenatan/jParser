package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.builder.JProcess;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class EmscriptenTarget extends DefaultBuildTarget {

    public static boolean SKIP_GLUE_CODE = false;
    public final static String EMSCRIPTEN_ROOT = (System.getenv("EMSDK") + "/upstream/emscripten/").replace("\\", "/").replace("//", "/");

    public IDLReader idlReader;

    public static boolean DEBUG_BUILD = false;
    public static boolean IS_WASM = true;
    public static boolean IS_X64 = false;

    public boolean isStatic = false;
    public boolean compileGlueCode = true;

    String WEBIDL_BINDER_SCRIPT = EMSCRIPTEN_ROOT + "tools/webidl_binder.py";

    public long initialMemory = 64 * 1024 * 1024;
    public long stackSize = 1048576;

    public ArrayList<String> exportedFunctions = new ArrayList<>();
    public ArrayList<String> exportedRuntimeMethods = new ArrayList<>();

    public String mainModuleName = null;

    public EmscriptenTarget() {
        this(SourceLanguage.CPP);
    }

    public EmscriptenTarget(SourceLanguage language) {
        this.libDirSuffix = "emscripten/";
        this.tempBuildDir = "target/emscripten/";

        String cppCompilerr = "";
        if(language == SourceLanguage.C) {
            cppCompilerr = EMSCRIPTEN_ROOT + "emcc";
        }
        else if(language == SourceLanguage.CPP) {
            cppCompilerr = EMSCRIPTEN_ROOT + "em++";
        }
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

        exportedFunctions.add("_free");
        exportedFunctions.add("_malloc");
        exportedFunctions.add("__Znwm");

        exportedRuntimeMethods.add("UTF8ToString");
        exportedRuntimeMethods.add("HEAP8");
        exportedRuntimeMethods.add("HEAPU8");
        exportedRuntimeMethods.add("HEAP16");
        exportedRuntimeMethods.add("HEAPU16");
        exportedRuntimeMethods.add("HEAP32");
        exportedRuntimeMethods.add("HEAPU32");
        exportedRuntimeMethods.add("HEAPF32");
        exportedRuntimeMethods.add("loadWebAssemblyModule");
//        exportedRuntimeMethods.add("LDSO");
//        exportedRuntimeMethods.add("asyncLoad");
//        exportedRuntimeMethods.add("loadDynamicLibrary");

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
            if(JParser.CREATE_IDL_HELPER) {
                headerDirs.add("-include" + idlHelperHFile.path());
            }
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

            if(IS_X64) {
                cppFlags.add("-s");
                cppFlags.add("MEMORY64=1");
            }
        }
        else {
            if(mainModuleName != null && !mainModuleName.isEmpty()) {
                linkerFlags.add("-sSIDE_MODULE=2");
                libSuffix = ".wasm";
                exportedFunctions.clear();
            }
            else {
                linkerFlags.add("-sMAIN_MODULE=2");
            }
            String postPath = createPostJS(jsglueDir, libName);
            linkerFlags.add("-s");
            linkerFlags.add("ALLOW_MEMORY_GROWTH=1");
            linkerFlags.add("-s");
            linkerFlags.add("ALLOW_TABLE_GROWTH=1");
            linkerFlags.add("-s");
            linkerFlags.add("MODULARIZE=1");
            linkerFlags.add("-s");
            linkerFlags.add("INITIAL_MEMORY=" + initialMemory);
            linkerFlags.add("-s");
            linkerFlags.add("STACK_SIZE=" + stackSize);
            linkerFlags.add("-s");
            linkerFlags.add("EXPORTED_FUNCTIONS=" + obtainList(exportedFunctions));
            linkerFlags.add("-s");
            linkerFlags.add("EXPORTED_RUNTIME_METHODS=" + obtainList(exportedRuntimeMethods));
            if(DEBUG_BUILD) {
                linkerFlags.add("-s");
                linkerFlags.add("ASSERTIONS=1");
                linkerFlags.add("-s");
                linkerFlags.add("SAFE_HEAP=1");
            }
            if(IS_WASM) {
                linkerFlags.add("-s");
                linkerFlags.add("WASM=1");

                if(IS_X64) {
                    linkerFlags.add("-s");
                    linkerFlags.add("WASM_BIGINT=1");
                }
                else {
                    linkerFlags.add("-s");
                    linkerFlags.add("WASM_BIGINT=0");
                }
            }
            else {
                linkerFlags.add("-s");
                linkerFlags.add("WASM=0");
            }
            if(IS_X64) {
                linkerFlags.add("-s");
                linkerFlags.add("MEMORY64=1");
                cppFlags.add("-s");
                cppFlags.add("MEMORY64=1");
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

        boolean success = super.build(config, buildTargetTemp);

        if(success && !isStatic) {
            if(mainModuleName != null && !mainModuleName.isEmpty()) {
                CustomFileDescriptor libDir = config.libDir.child(libDirSuffix);
                createSideModule(jsglueDir, libName, libDir);
            }
        }

        return success;
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

    private void createSideModule(CustomFileDescriptor jsglueDir, String libName, CustomFileDescriptor libDir) {
        CustomFileDescriptor sideModuleFile = new CustomFileDescriptor("emscripten/sidemodule.js", CustomFileDescriptor.FileType.Classpath);
        CustomFileDescriptor glueFile = jsglueDir.child("glue.js");
        String glueText = glueFile.readString();
        String s = sideModuleFile.readString();
        s = s.replace("[MAIN_MODULE_NAME]", mainModuleName);
        s = s.replace("[SIDE_MODULE_NAME]", libName);
        s = s.replace("[SIDE_MODULE_WASM]", libName + ".wasm");
        s = s.replace("[GLUE_CODE]", glueText);
        CustomFileDescriptor wasmFile = libDir.child(libName + ".wasm");
        byte[] wasmBytes = wasmFile.readBytes();
        String base64 = java.util.Base64.getEncoder().encodeToString(wasmBytes);
        s = s.replace("[WASM_BIN]", base64);
        s = minifyJS(s);
        CustomFileDescriptor jsFile = libDir.child(libName + ".wasm.js");
        jsFile.writeString(s, false);
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
        return JProcess.startProcess(jsglueDir.file(), generateGlueCommand, environment);
    }

    private CustomFileDescriptor mergeIDLFile(CustomFileDescriptor jsglueDir) {
        String idlStr = idlReader.mergeIDLFiles();
        CustomFileDescriptor mergedIdlFile = jsglueDir.child("IDLMerged.idl");
        mergedIdlFile.writeString(idlStr, false);
        return mergedIdlFile;
    }

    private String minifyJS(String js) {
        js = js.replaceAll("/\\*.*?\\*/", "");
        js = js.replaceAll("//.*", "");
        js = js.replaceAll("\\s+", " ");
        js = js.replaceAll("\\s*([{}();,])\\s*", "$1");
        return js.trim();
    }

    private String obtainList(ArrayList<String> list) {
        String items = "[";
        int size = list.size();
        for(int i = 0; i < size; i++) {
            String item = "'" + list.get(i) + "'";
            items += item;
            if(i < size - 1) {
                items += ", ";
            }
        }
        items += "]";
        return items;
    }
}
