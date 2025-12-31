package com.github.xpenatan.jParser.builder.targets;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.builder.JProcess;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmscriptenTarget extends DefaultBuildTarget {

    public static boolean SKIP_GLUE_CODE = false;
    public final static String UPSTREAM_ROOT = (System.getenv("EMSDK") + "/upstream/").replace("\\", "/").replace("//", "/");
    public final static String UPSTREAM_BIN = (UPSTREAM_ROOT + "/bin/").replace("\\", "/").replace("//", "/");
    public final static String EMSCRIPTEN_ROOT = (UPSTREAM_ROOT + "/emscripten/").replace("\\", "/").replace("//", "/");

    public IDLReader idlReader;

    public static boolean DEBUG_BUILD = false;
    public static boolean IS_WASM = true;
    public static boolean IS_X64 = false;

    public boolean compileGlueCode = true;

    String WEBIDL_BINDER_SCRIPT = EMSCRIPTEN_ROOT + "tools/webidl_binder.py";

    public long initialMemory = 64 * 1024 * 1024;
    public long stackSize = 1048576;

    public ArrayList<String> exportedFunctions = new ArrayList<>();
    public ArrayList<String> exportedRuntimeMethods = new ArrayList<>();

    public String mainModuleName = null;

    public SymbolsCallback allowSymbolsCallback = null;

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
        libSuffix = ".js";

        cppFlags.add("-c");
        cppFlags.add("-flto");

        exportedRuntimeMethods.add("UTF8ToString");
        exportedRuntimeMethods.add("HEAP8");
        exportedRuntimeMethods.add("HEAPU8");
        exportedRuntimeMethods.add("HEAP16");
        exportedRuntimeMethods.add("HEAPU16");
        exportedRuntimeMethods.add("HEAP32");
        exportedRuntimeMethods.add("HEAPU32");
        exportedRuntimeMethods.add("HEAPF32");
        exportedRuntimeMethods.add("loadDynamicLibrary");
        exportedRuntimeMethods.add("loadWebAssemblyModule");
        exportedRuntimeMethods.add("LDSO");
        exportedRuntimeMethods.add("wasmMemory");
        exportedRuntimeMethods.add("intArrayFromString");
        exportedRuntimeMethods.add("alignMemory");
        exportedRuntimeMethods.add("LDSO");
        exportedRuntimeMethods.add("asyncLoad");

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

        CustomFileDescriptor jsglueDir = config.buildRootGenSourcePath.child("jsglue");
        if(!jsglueDir.exists()) {
            jsglueDir.mkdirs();
        }

        if(compileGlueCode && !isStatic) {
            cppInclude.add("**/jsglue/*.cpp");
        }

        if(idlReader != null) {
            CustomFileDescriptor mergedIDLFile = mergeIDLFile(jsglueDir);
            if(!SKIP_GLUE_CODE && !createGlueCode(mergedIDLFile, jsglueDir)) {
                return false;
            }
        }

        if(isStatic) {
            linkerCompiler.clear();
            linkerOutputCommand = "";

            String cppCompilerr = EMSCRIPTEN_ROOT + "emar";
            if(isWindows()) {
                cppCompilerr += ".bat";
            }

            linkerCompiler.add(cppCompilerr);
            linkerFlags.add("rcs");
            libSuffix = "_.a";
            if(IS_X64) {
                cppFlags.add("-sMEMORY64=1");
            }
        }
        else {
            linkerOutputCommand = "-o";
            if(mainModuleName != null && !mainModuleName.isEmpty()) {
                boolean fail = true;
                for(String flag : linkerFlags) {
                    if(flag.contains("SIDE_MODULE=")) {
                        fail = false;
                        break;
                    }
                }
                if(fail) {
                    throw new RuntimeException("EmscriptenTarget: When using mainModuleName you have to set SIDE_MODULE 1 or 2 in linkerFlags");
                }
                libSuffix = ".wasm";
            }
            linkerFlags.add("-sALLOW_MEMORY_GROWTH=1");
            linkerFlags.add("-sALLOW_TABLE_GROWTH=1");
            linkerFlags.add("-sMODULARIZE=1");
            linkerFlags.add("-sINITIAL_MEMORY=" + initialMemory);
            linkerFlags.add("-sSTACK_SIZE=" + stackSize);
            CustomFileDescriptor exportedFunctionsFile = config.buildRootPath.child("target/emscripten/static/exported_functions.txt");
            mergeExportedFunctionsToSymbols(exportedFunctionsFile, exportedFunctions);
            linkerFlags.add("-sEXPORTED_FUNCTIONS=@" + exportedFunctionsFile.path());
            linkerFlags.add("-sEXPORTED_RUNTIME_METHODS=" + obtainList(exportedRuntimeMethods));
            if(DEBUG_BUILD) {
                linkerFlags.add("-sASSERTIONS=1");
                linkerFlags.add("-sSAFE_HEAP=1");
            }
            if(IS_WASM) {
                linkerFlags.add("-sWASM=1");

//                if(IS_X64) {
                    linkerFlags.add("-sWASM_BIGINT=1");
//                }
//                else {
//                    linkerFlags.add("-sWASM_BIGINT=0");
//                }
            }
            else {
                linkerFlags.add("-sWASM=0");
            }
            if(IS_X64) {
                linkerFlags.add("-sMEMORY64=1");
                cppFlags.add("-sMEMORY64=1");
            }

            linkerFlags.add("--post-js");
            linkerFlags.add(jsglueDir.path() + "/glue.js");
            linkerFlags.add("-sEXPORT_NAME='" + libName + "'");
        }

        boolean success = super.build(config, buildTargetTemp);

        if(success) {
            if(isStatic) {
                generateSymbols(buildTargetTemp, config);
            }
            else {
                if(mainModuleName != null && !mainModuleName.isEmpty()) {
                    CustomFileDescriptor libDir = config.compiledLibsPath.child(libDirSuffix);
                    createSideModule(jsglueDir, libName, libDir);
                }
            }
        }

        return success;
    }

    private void createSideModule(CustomFileDescriptor jsglueDir, String libName, CustomFileDescriptor libDir) {
        CustomFileDescriptor sideModuleFile = new CustomFileDescriptor("emscripten/sidemodule.js", CustomFileDescriptor.FileType.Classpath);
        CustomFileDescriptor glueFile = jsglueDir.child("glue.js");
        String glueText = glueFile.readString();
        String s = sideModuleFile.readString();
        s = s.replace("[MAIN_MODULE_NAME]", "window." + mainModuleName);
        s = s.replace("[SIDE_MODULE_NAME]", libName);
        s = s.replace("[SIDE_MODULE_WASM]", libName + ".wasm");
        s = s.replace("[GLUE_CODE]", glueText);
        s = removeJSCode(s);
        s = replaceMethodInSideModule(s, "window." + mainModuleName + ".");
//        s = minifyJS(s);
        CustomFileDescriptor jsFile = libDir.child(libName + ".js");
        jsFile.writeString(s, false);
    }

    private String replaceMethodInSideModule(String js, String prefix) {
        js = mapMethodInSideModule(js, prefix, "intArrayFromString");
        js = mapMethodInSideModule(js, prefix, "alignMemory");
        js = mapMethodInSideModule(js, prefix, "HEAP8");
        js = mapMethodInSideModule(js, prefix, "HEAP16");
        js = mapMethodInSideModule(js, prefix, "HEAPU8");
        js = mapMethodInSideModule(js, prefix, "HEAPU16");
        js = mapMethodInSideModule(js, prefix, "HEAP32");
        js = mapMethodInSideModule(js, prefix, "HEAPU32");
        js = mapMethodInSideModule(js, prefix, "HEAPF32");
        js = mapMethodInSideModule(js, prefix, "HEAPF64");
        js = mapMethodInSideModule(js, prefix, "UTF8ToString");
        js = mapMethodInSideModule(js, prefix, "WrapperObject");
        js = mapMethodInSideModule(js, prefix, "getCache");
        js = mapMethodInSideModule(js, prefix, "castObject");
        js = mapMethodInSideModule(js, prefix, "getClass");
//        js = mapMethodInSideModule(js, prefix, "destroy");
//        js = mapMethodInSideModule(js, prefix, "getPointer");
//        js = mapMethodInSideModule(js, prefix, "wrapPointer");
        return js;
    }

    private String removeJSCode(String js) {
        js = js.replaceAll("function WrapperObject\\(\\) \\{\\s*\\}\\s*WrapperObject\\.prototype = Object\\.create\\(WrapperObject\\.prototype\\);\\s*WrapperObject\\.prototype\\.constructor = WrapperObject;\\s*WrapperObject\\.prototype\\.__class__ = WrapperObject;\\s*", "");
        js = js.replaceAll("function getCache\\(__class__\\) \\{\\s*return \\(__class__ \\|\\| WrapperObject\\)\\.__cache__;\\s*\\}", "");
        js = js.replaceAll("function castObject\\(obj, __class__\\) \\{\\s*return wrapPointer\\(obj\\.ptr, __class__\\);\\s*\\}", "");
        js = js.replaceAll("function getClass\\(obj\\) \\{\\s*return obj\\.__class__;\\s*\\}", "");
        js = js.replace("WrapperObject.__cache__ = {};", "");
        js = js.replace("Module['WrapperObject'] = WrapperObject;", "");
        js = js.replace("Module['getCache'] = getCache;", "");
        js = js.replace("Module['castObject'] = castObject;", "");
        js = js.replace("Module['getClass'] = getClass;", "");
        js = js.replace("Module['NULL'] = wrapPointer(0);", "");
        js = js.replaceAll("(?s)/\\*.*?\\*/", "");
        js = js.replaceAll("//.*", "");
        js = js.replaceAll("(\\r?\\n){3,}", "\n\n");
        return js;
    }

    private String mapMethodInSideModule(String js, String prefix, String method) {
        if(js == null || js.isEmpty() || prefix == null || prefix.isEmpty() || method == null || method.isEmpty()) {
            return js;
        }
        String[] avoidPrefixes = {"_", "'", "."};
        Pattern pattern = Pattern.compile(Pattern.quote(method));
        Matcher matcher = pattern.matcher(js);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            boolean avoid = false;
            int start = matcher.start();
            for (String avoidPrefix : avoidPrefixes) {
                if (start >= avoidPrefix.length() && js.substring(start - avoidPrefix.length(), start).equals(avoidPrefix)) {
                    avoid = true;
                    break;
                }
            }
            if (!avoid) {
                sb.append(js.substring(lastEnd, start)).append(prefix).append(method);
            } else {
                sb.append(js.substring(lastEnd, matcher.end()));
            }
            lastEnd = matcher.end();
        }
        sb.append(js.substring(lastEnd));
        return sb.toString();
    }

    private boolean createGlueCode(CustomFileDescriptor mergedIDLFile, CustomFileDescriptor jsglueDir) {
        String pythonCmd = "python";
        if(isUnix()) {
            pythonCmd = "python3";
        }

        CustomFileDescriptor WEBIDL_BINDER_FILE = new CustomFileDescriptor(WEBIDL_BINDER_SCRIPT, CustomFileDescriptor.FileType.Absolute);
        String webIDLBinder = WEBIDL_BINDER_FILE.readString();

        CustomFileDescriptor updatedWEBIDLBinder = jsglueDir.child("webidl_binder.py");
        updatedWEBIDLBinder.writeString(webIDLBinder, false);

        ArrayList<String> generateGlueCommand = new ArrayList<>();
        generateGlueCommand.add(pythonCmd);
        generateGlueCommand.add(updatedWEBIDLBinder.name());
        generateGlueCommand.add(mergedIDLFile.toString());
        generateGlueCommand.add("glue");

        HashMap<String, String> environment = new HashMap<>();
        String pathSep = isWindows() ? ";" : ":";
        String currentPythonPath = environment.get("PYTHONPATH");
        if (currentPythonPath == null) currentPythonPath = "";
        if (!currentPythonPath.isEmpty()) currentPythonPath += pathSep;
        currentPythonPath += EMSCRIPTEN_ROOT;
        environment.put("PYTHONPATH", currentPythonPath);

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

    private void generateSymbols(CustomFileDescriptor buildTargetTemp, BuildConfig config) {
        CustomFileDescriptor objList = buildTargetTemp.child("objs.txt");
        if(!objList.exists()) return;
        String content = objList.readString();
        String[] objs = content.split("\n");
        HashSet<String> allSymbols = new HashSet<>();
        for(String obj : objs) {
            obj = obj.trim();
            if(obj.isEmpty()) continue;
            ArrayList<String> symbols = getSymbols(obj, config);
            allSymbols.addAll(symbols);
        }

        HashSet<String> allowedSymbols = new HashSet<>();
        for(String symbol : allSymbols) {
            if(allowSymbolsCallback != null) {
                if(allowSymbolsCallback.allowSymbol(symbol)) {
                    allowedSymbols.add(symbol);
                }
            }
        }

        writeAllSymbols(buildTargetTemp, allSymbols);
        writeAllowedSymbols1(buildTargetTemp, allowedSymbols);
    }

    private void writeAllSymbols(CustomFileDescriptor buildTargetTemp, HashSet<String> allowedSymbols) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(String symbol : allowedSymbols) {
            sb.append(symbol);
            sb.append("\n");
        }
        CustomFileDescriptor symbolsFile = buildTargetTemp.child("symbols.txt");
        symbolsFile.writeString(sb.toString(), false);
    }

    private void writeAllowedSymbols1(CustomFileDescriptor buildTargetTemp, HashSet<String> allowedSymbols) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for(String symbol : allowedSymbols) {
            if(!first) sb.append(", ");
            sb.append("\"").append(symbol).append("\"");
            first = false;
        }
        sb.append("]");
        CustomFileDescriptor exportedFunctionsFile = buildTargetTemp.child("exported_functions.txt");
        exportedFunctionsFile.writeString(sb.toString(), false);
    }

    private ArrayList<String> getSymbols(String objPath, BuildConfig config) {
        ArrayList<String> symbols = new ArrayList<>();
        try {
            String llvmNm = UPSTREAM_BIN + "llvm-nm";
            if(isWindows()) llvmNm += ".exe";
            ProcessBuilder pb = new ProcessBuilder(llvmNm, objPath);
            pb.environment().putAll(environment);
            pb.directory(config.buildRootPath.file());
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if(parts.length >= 3) {
                    String symbol = parts[parts.length - 1];
                    symbols.add(symbol);
                }
            }
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((errorReader.readLine()) != null) {
                // consume error stream
            }
            p.waitFor();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return symbols;
    }

    private void mergeExportedFunctionsToSymbols(CustomFileDescriptor symbolsFile, ArrayList<String> exportedFunctions) {
        String existing = "[]";
        if(symbolsFile.exists()) {
            existing = symbolsFile.readString().trim();
        }
        LinkedHashSet<String> allSymbols = new LinkedHashSet<>();
        for(String func : exportedFunctions) {
            allSymbols.add(func);
        }
        if(existing.length() > 2) {
            String content = existing.substring(1, existing.length() - 1);
            String[] parts = content.split(", ");
            for(String part : parts) {
                if(part.startsWith("\"") && part.endsWith("\"")) {
                    String symbol = part.substring(1, part.length() - 1);
                    if(!allSymbols.contains(symbol)) {
                        allSymbols.add(symbol);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for(String symbol : allSymbols) {
            if(!first) sb.append(", ");
            sb.append("\"").append(symbol).append("\"");
            first = false;
        }
        sb.append("]");
        symbolsFile.writeString(sb.toString(), false);
    }

    public interface SymbolsCallback {
        boolean allowSymbol(String symbol);
    }
}
