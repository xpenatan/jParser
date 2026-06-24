package com.github.xpenatan.jParser.ffm;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Generates FFMGlue.cpp/.h with extern "C" exported functions using standard C types.
 * Parallel to NativeCPPGenerator but without any JNI dependencies.
 */
public class FFMCppGenerator implements FFMNativeCodeGenerator {

    public static boolean SKIP_GLUE_CODE = false;

    private String glueCppDestinationDir;
    private String cppGlueName = "FFMGlue";
    private String exportMacroName = "FFM_EXPORT";
    private FFMClassData ffmClassData;

    StringBuilder mainPrinter = new StringBuilder();
    StringBuilder headerPrinter = new StringBuilder();
    StringBuilder codePrinter = new StringBuilder();
    private final TreeMap<String, String> obfuscationMapping = new TreeMap<>();

    private boolean init = true;

    public FFMCppGenerator(String cppDestinationDir) {
        this(cppDestinationDir, "ffmglue", "FFMGlue", "FFM_EXPORT");
    }

    protected FFMCppGenerator(String cppDestinationDir, String glueDirName, String cppGlueName, String exportMacroName) {
        this.cppGlueName = cppGlueName;
        this.exportMacroName = exportMacroName;
        try {
            this.glueCppDestinationDir = new File(cppDestinationDir, glueDirName).getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFFMClassData(FFMClassData ffmClassData) {
        this.ffmClassData = ffmClassData;
    }

    private void print(PrintType type, String text) {
        if(init) {
            init = false;
            headerPrinter.append("#pragma once\n");
            headerPrinter.append("#include <cstdint>\n");
            headerPrinter.append("\n");
            headerPrinter.append("#ifdef _WIN32\n");
            headerPrinter.append("    #define " + exportMacroName + " __declspec(dllexport)\n");
            headerPrinter.append("#else\n");
            headerPrinter.append("    #define " + exportMacroName + " __attribute__((visibility(\"default\")))\n");
            headerPrinter.append("#endif\n");
            headerPrinter.append("\n");
            mainPrinter.append("\n");
            mainPrinter.append("extern \"C\" {\n");
            mainPrinter.append("\n");
        }
        if(type == PrintType.HEADER) {
            headerPrinter.append(text + "\n");
        }
        else if(type == PrintType.MAIN) {
            mainPrinter.append(text + "\n");
        }
        else if(type == PrintType.CODE) {
            codePrinter.append(text + "\n");
        }
    }

    @Override
    public void addNativeCode(Node node, String content) {
        Scanner scanner = new Scanner(content);
        boolean haveInclude = content.contains("#include");
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if(haveInclude) {
                print(PrintType.HEADER, line);
            }
            else {
                print(PrintType.CODE, line);
            }
        }
        scanner.close();
    }

    @Override
    public void addCallbackClassCode(String cppClassCode) {
        // Callback class code goes into the CODE section (before extern "C")
        print(PrintType.CODE, cppClassCode);
    }

    @Override
    public void addNativeCode(MethodDeclaration nativeMethod, String content) {
        String methodName = nativeMethod.getNameAsString();
        TypeDeclaration classOrEnum = (TypeDeclaration) nativeMethod.getParentNode().get();
        CompilationUnit compilationUnit = classOrEnum.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classOrEnum.getNameAsString();
        boolean overloadedMethod = isOverloadedMethod(classOrEnum, methodName);
        String returnTypeStr = nativeMethod.getType().toString();
        String returnType = FFMTypeMapper.getCType(returnTypeStr);

        // Build parameter list — no JNIEnv*, no jclass/jobject
        String params = "(";
        ArrayList<FFMArgument> arguments = new ArrayList<>();
        if(nativeMethod.getParameters() != null) {
            for(Parameter parameter : nativeMethod.getParameters()) {
                FFMArgument argument = getArgument(parameter);
                arguments.add(argument);
            }
        }

        String prefixCode = "";
        String suffixCode = "";

        for(int i = 0; i < arguments.size(); i++) {
            FFMArgument argument = arguments.get(i);
            String paramName = argument.name;
            String cType = argument.cType;
            if(i > 0) {
                params += ", ";
            }

            // Strings arrive as const char* directly from FFM — no conversion needed
            params += cType + " " + paramName;
        }

        params += ")";

        boolean haveReturn = containsReturnStatement(content);
        if(haveReturn) {
            String wrappedLambda = "" +
                    returnType + " wrappedReturn = [&]() -> " + returnType + " {\n" +
                    content +
                    "\n }();";

            content = wrappedLambda;
            suffixCode += "return wrappedReturn;";
        }

        content = prefixCode + "\n" + content + "\n" + suffixCode;

        String symbolName = buildSymbolName(packageName, className, methodName, arguments, overloadedMethod, ffmClassData);
        registerObfuscationMapping(nativeMethod, packageName, className, symbolName, arguments);
        print(PrintType.MAIN, exportMacroName + " " + returnType + " " + symbolName + params + " {");
        content = "\t" + content.replace("\n", "\n\t");
        print(PrintType.MAIN, content);
        print(PrintType.MAIN, "}");
        print(PrintType.MAIN, "");
    }

    private boolean containsReturnStatement(String content) {
        Scanner scanner = new Scanner(content);
        try {
            while(scanner.hasNextLine()) {
                if(scanner.nextLine().trim().startsWith("return ")) {
                    return true;
                }
            }
            return false;
        }
        finally {
            scanner.close();
        }
    }

    /**
     * Build the symbol name for a native method.
     * Must match exactly with what FFMCodeParser generates for SymbolLookup.find().
     */
    public static String buildSymbolName(String packageName, String className, String methodName, ArrayList<FFMArgument> arguments, boolean overloadedMethod, FFMClassData ffmClassData) {
        FFMClassData.SymbolNameMode mode = ffmClassData != null ? ffmClassData.symbolNameMode : FFMClassData.SymbolNameMode.DEFAULT;
        if(mode == FFMClassData.SymbolNameMode.OBFUSCATED) {
            return buildObfuscatedSymbolName(packageName, className, methodName, arguments, ffmClassData);
        }
        return buildCompactSymbolName(packageName, className, methodName, arguments, overloadedMethod);
    }

    private static String buildCompactSymbolName(String packageName, String className, String methodName, ArrayList<FFMArgument> arguments, boolean overloadedMethod) {
        String packageToken = sanitizePackageToken(packageName);
        String classToken = sanitizeToken(className);
        String methodToken = sanitizeToken(stripNativeMarker(methodName));
        String base = packageToken + "_" + classToken + "_" + methodToken;
        if(overloadedMethod) {
            return base + "_" + buildCompactSignatureToken(arguments);
        }
        return base;
    }

    private static String buildObfuscatedSymbolName(String packageName, String className, String methodName, ArrayList<FFMArgument> arguments, FFMClassData ffmClassData) {
        String salt = ffmClassData != null && ffmClassData.symbolObfuscationSalt != null ? ffmClassData.symbolObfuscationSalt : "";
        StringBuilder fingerprint = new StringBuilder();
        fingerprint.append(packageName).append('|').append(className).append('|').append(methodName).append('|');
        for(int i = 0; i < arguments.size(); i++) {
            if(i > 0) {
                fingerprint.append(',');
            }
            fingerprint.append(arguments.get(i).javaType);
        }
        return "n" + Integer.toUnsignedString((salt + "|" + fingerprint).hashCode());
    }

    private static String buildCompactSignatureToken(ArrayList<FFMArgument> arguments) {
        if(arguments.isEmpty()) {
            return "v";
        }
        StringBuilder out = new StringBuilder(arguments.size() * 3);
        for(int i = 0; i < arguments.size(); i++) {
            if(i > 0) {
                out.append('_');
            }
            out.append(compactTypeToken(arguments.get(i).javaType));
        }
        return out.toString();
    }

    private static String compactTypeToken(String javaType) {
        switch(javaType) {
            case "boolean": return "z";
            case "byte": return "b";
            case "char": return "c";
            case "short": return "s";
            case "int": return "i";
            case "long": return "l";
            case "float": return "f";
            case "double": return "d";
            case "String": return "str";
            case "MemorySegment":
            case "java.lang.foreign.MemorySegment": return "addr";
            default:
                if(javaType.endsWith("[]")) {
                    return compactTypeToken(javaType.substring(0, javaType.length() - 2)) + "a";
                }
                return "obj";
        }
    }

    private static String sanitizeToken(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                out.append(Character.toLowerCase(c));
            }
            else if(c == '_' || c == '$') {
                out.append('_');
            }
        }
        if(out.length() == 0) {
            return "x";
        }
        return out.toString();
    }

    private static String stripNativeMarker(String methodName) {
        String marker = "internal_native_";
        if(methodName.startsWith(marker)) {
            return methodName.substring(marker.length());
        }
        return methodName;
    }

    private static String sanitizePackageToken(String packageName) {
        String[] tokens = packageName.split("\\.");
        StringBuilder out = new StringBuilder(packageName.length());
        for(int i = 0; i < tokens.length; i++) {
            if(i > 0) {
                out.append('_');
            }
            out.append(sanitizeToken(tokens[i]));
        }
        return out.toString();
    }

    private static boolean isOverloadedMethod(TypeDeclaration classOrEnum, String methodName) {
        int count = 0;
        if(classOrEnum.isClassOrInterfaceDeclaration()) {
            count = classOrEnum.asClassOrInterfaceDeclaration().getMethodsByName(methodName).size();
        }
        else if(classOrEnum.isEnumDeclaration()) {
            count = classOrEnum.asEnumDeclaration().getMethodsByName(methodName).size();
        }
        return count > 1;
    }

    private void registerObfuscationMapping(MethodDeclaration nativeMethod, String packageName, String className, String symbolName, ArrayList<FFMArgument> arguments) {
        if(!isObfuscatedMode()) {
            return;
        }
        StringBuilder signature = new StringBuilder();
        signature.append(packageName)
                .append('.')
                .append(className)
                .append('#')
                .append(nativeMethod.getNameAsString())
                .append('(');
        for(int i = 0; i < arguments.size(); i++) {
            if(i > 0) {
                signature.append(", ");
            }
            signature.append(arguments.get(i).javaType);
        }
        signature.append(") : ")
                .append(nativeMethod.getType().toString());
        obfuscationMapping.put(symbolName, signature.toString());
    }

    private boolean isObfuscatedMode() {
        FFMClassData.SymbolNameMode mode = ffmClassData != null ? ffmClassData.symbolNameMode : FFMClassData.SymbolNameMode.DEFAULT;
        return mode == FFMClassData.SymbolNameMode.OBFUSCATED;
    }

    private void writeObfuscationMappingFile(String gluePathStr) {
        if(!isObfuscatedMode() || SKIP_GLUE_CODE) {
            return;
        }
        String mapPath = gluePathStr + cppGlueName + ".mapping.txt";
        CustomFileDescriptor mappingFile = new CustomFileDescriptor(mapPath);
        StringBuilder mapText = new StringBuilder();
        mapText.append("# FFM obfuscation mapping\n");
        mapText.append("# <symbol> = <package>.<class>#<method>(<params>) : <return>\n");
        for(Map.Entry<String, String> entry : obfuscationMapping.entrySet()) {
            mapText.append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue())
                    .append('\n');
        }
        mappingFile.writeString(mapText.toString(), false);
    }

    @Override
    public void addParseFile(JParser jParser, JParserItem parserItem) {
    }

    @Override
    public void generate(JParser jParser) {
        headerPrinter.append("\n");

        mainPrinter.insert(0, codePrinter);
        mainPrinter.insert(0, headerPrinter);
        print(PrintType.MAIN, "}");
        String code = mainPrinter.toString();

        String gluePathStr = glueCppDestinationDir;
        String cppGlueHPath = gluePathStr + cppGlueName + ".h";
        String cppGluePath = gluePathStr + cppGlueName + ".cpp";
        CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(cppGlueHPath);
        if(!SKIP_GLUE_CODE) {
            fileDescriptor.writeString(code, false);
        }

        CustomFileDescriptor cppFile = new CustomFileDescriptor(cppGluePath);
        String include = "#include \"" + cppGlueName + ".h\"";
        cppFile.writeString(include, false);
        writeObfuscationMappingFile(gluePathStr);
    }

    private FFMArgument getArgument(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
        String cType = FFMTypeMapper.getCType(type);
        String overloadSuffix = FFMTypeMapper.getOverloadSuffix(type);
        return new FFMArgument(parameter.getNameAsString(), type, cType, overloadSuffix);
    }

    /**
     * Represents a function argument with its FFM/C type info.
     */
    public static class FFMArgument {
        public final String name;
        public final String javaType;
        public final String cType;
        public final String overloadSuffix;

        public FFMArgument(String name, String javaType, String cType, String overloadSuffix) {
            this.name = name;
            this.javaType = javaType;
            this.cType = cType;
            this.overloadSuffix = overloadSuffix;
        }
    }

    enum PrintType {
        HEADER,
        CODE,
        MAIN
    }
}


