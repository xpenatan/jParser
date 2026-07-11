package com.github.xpenatan.jParser.c;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Generates the portable TeaVM C direct-import, plugin ABI, and runtime dispatch artifacts. */
final class TeaVMCRuntimeArtifacts {

    static final String ABI_DIRECTORY = "teavmcabi";
    static final String ABI_HEADER = "TeaVMCAbi.h";
    static final String DIRECT_IMPORT_HEADER = "teavmc_imports.h";
    static final String DISPATCH_HEADER = "TeaVMCDispatch.h";
    static final String DISPATCH_SOURCE = "TeaVMCDispatch.cpp";
    static final String ABI_METADATA = "teavmc_abi.properties";

    private static final String EXPORT_TOKEN = "TEAVMC_EXPORT";
    private static final Pattern LAST_IDENTIFIER = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*$");
    private static final Pattern CALLBACK_TYPEDEF = Pattern.compile(
            "typedef\\s+[^;]*?\\(\\s*\\*\\s*fp_[A-Za-z_][A-Za-z0-9_]*\\s*\\)[^;]*;",
            Pattern.DOTALL);

    private TeaVMCRuntimeArtifacts() {
    }

    static void generate(Path cppDestinationRoot, String libraryName) throws IOException {
        Path glueHeader = cppDestinationRoot.resolve("teavmcglue").resolve("TeaVMCGlue.h");
        if(!Files.isRegularFile(glueHeader)) {
            throw new IOException("Missing generated TeaVM C glue header: " + glueHeader);
        }

        String originalGlue = new String(Files.readAllBytes(glueHeader), StandardCharsets.UTF_8);
        List<String> callbackTypedefs = extractCallbackTypedefs(originalGlue);
        List<ExportSymbol> symbols = extractExportSymbols(originalGlue);
        String logicalName = normalizeLogicalName(libraryName);
        String identifier = cIdentifier(logicalName);
        Fingerprint fingerprint = fingerprint(logicalName, callbackTypedefs, symbols);

        Path abiRoot = cppDestinationRoot.resolve(ABI_DIRECTORY);
        Files.createDirectories(abiRoot);
        write(abiRoot.resolve(DIRECT_IMPORT_HEADER), directImportHeader(identifier, callbackTypedefs, symbols));
        write(abiRoot.resolve(ABI_HEADER), abiHeader(identifier, callbackTypedefs, symbols));
        write(abiRoot.resolve(DISPATCH_HEADER), dispatchHeader(identifier, symbols));
        write(abiRoot.resolve(DISPATCH_SOURCE), dispatchSource(logicalName, identifier, symbols, fingerprint));
        write(abiRoot.resolve(ABI_METADATA), metadata(logicalName, identifier, symbols, fingerprint));

        String provider = pluginProvider(logicalName, identifier, symbols, fingerprint);
        write(glueHeader, originalGlue + provider);
    }

    private static List<String> extractCallbackTypedefs(String glue) {
        Map<String, String> typedefs = new LinkedHashMap<>();
        Matcher matcher = CALLBACK_TYPEDEF.matcher(glue);
        while(matcher.find()) {
            String declaration = normalizeWhitespace(matcher.group());
            typedefs.put(declaration, declaration);
        }
        ArrayList<String> result = new ArrayList<>(typedefs.values());
        Collections.sort(result);
        return result;
    }

    private static List<ExportSymbol> extractExportSymbols(String glue) throws IOException {
        Map<String, ExportSymbol> symbols = new LinkedHashMap<>();
        int searchFrom = 0;
        while(true) {
            int exportStart = glue.indexOf(EXPORT_TOKEN, searchFrom);
            if(exportStart < 0) {
                break;
            }
            searchFrom = exportStart + EXPORT_TOKEN.length();
            int lineStart = glue.lastIndexOf('\n', exportStart);
            lineStart = lineStart < 0 ? 0 : lineStart + 1;
            String linePrefix = glue.substring(lineStart, exportStart).trim();
            if(linePrefix.startsWith("#define") || linePrefix.startsWith("//")) {
                continue;
            }

            int openParen = glue.indexOf('(', searchFrom);
            if(openParen < 0) {
                continue;
            }
            String declarationPrefix = normalizeWhitespace(glue.substring(searchFrom, openParen));
            Matcher nameMatcher = LAST_IDENTIFIER.matcher(declarationPrefix);
            if(!nameMatcher.find()) {
                continue;
            }
            String symbolName = nameMatcher.group(1);
            String returnType = normalizeWhitespace(declarationPrefix.substring(0, nameMatcher.start()));
            if(returnType.isEmpty() || returnType.startsWith("__declspec")) {
                continue;
            }

            int closeParen = matchingParen(glue, openParen);
            if(closeParen < 0) {
                throw new IOException("Unterminated TeaVM C export parameter list for " + symbolName);
            }
            int bodyStart = skipWhitespace(glue, closeParen + 1);
            if(bodyStart >= glue.length() || glue.charAt(bodyStart) != '{') {
                continue;
            }

            String parameters = normalizeWhitespace(glue.substring(openParen + 1, closeParen));
            ExportSymbol symbol = new ExportSymbol(symbolName, returnType, parameters);
            ExportSymbol previous = symbols.putIfAbsent(symbolName, symbol);
            if(previous != null && !previous.canonicalSignature().equals(symbol.canonicalSignature())) {
                throw new IOException("TeaVM C export symbol collision for " + symbolName + ": "
                        + previous.canonicalSignature() + " vs " + symbol.canonicalSignature());
            }
            searchFrom = closeParen + 1;
        }

        ArrayList<ExportSymbol> result = new ArrayList<>(symbols.values());
        result.sort(Comparator.comparing((ExportSymbol symbol) -> symbol.name)
                .thenComparing(ExportSymbol::canonicalSignature));
        return result;
    }

    private static int matchingParen(String value, int openParen) {
        int depth = 0;
        for(int i = openParen; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '(') {
                depth++;
            }
            else if(c == ')') {
                depth--;
                if(depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int skipWhitespace(String value, int offset) {
        while(offset < value.length() && Character.isWhitespace(value.charAt(offset))) {
            offset++;
        }
        return offset;
    }

    private static String directImportHeader(String identifier, List<String> callbackTypedefs, List<ExportSymbol> symbols) {
        String guard = "JPARSER_TEAVMC_" + identifier.toUpperCase(Locale.ROOT) + "_DIRECT_IMPORTS_H";
        StringBuilder out = commonHeaderStart(guard);
        appendCallbackTypedefs(out, callbackTypedefs);
        out.append("#ifdef __cplusplus\nextern \"C\" {\n#endif\n\n");
        for(ExportSymbol symbol : symbols) {
            out.append(symbol.prototype()).append('\n');
        }
        out.append("\n#ifdef __cplusplus\n}\n#endif\n\n#endif\n");
        return out.toString();
    }

    private static String abiHeader(String identifier, List<String> callbackTypedefs, List<ExportSymbol> symbols) {
        String guard = "JPARSER_TEAVMC_" + identifier.toUpperCase(Locale.ROOT) + "_ABI_H";
        String typePrefix = typePrefix(identifier);
        StringBuilder out = commonHeaderStart(guard);
        out.append("/* The loader header defines this common prefix when it is available. */\n");
        out.append("#ifndef JPARSER_TEAVMC_PLUGIN_API_MAGIC\n");
        out.append("#define JPARSER_TEAVMC_PLUGIN_API_MAGIC UINT64_C(0x4A50544341504931)\n");
        out.append("#define JPARSER_TEAVMC_PLUGIN_ABI_MAJOR UINT16_C(1)\n");
        out.append("#define JPARSER_TEAVMC_PLUGIN_ABI_MINOR UINT16_C(0)\n");
        out.append("typedef struct JParserTeaVMCPluginApiHeader {\n");
        out.append("    uint64_t magic;\n");
        out.append("    uint16_t abi_major;\n");
        out.append("    uint16_t abi_minor;\n");
        out.append("    uint32_t header_size;\n");
        out.append("    uint32_t api_size;\n");
        out.append("    uint32_t symbol_count;\n");
        out.append("    uint64_t fingerprint_hi;\n");
        out.append("    uint64_t fingerprint_lo;\n");
        out.append("    const char* logical_name;\n");
        out.append("    const char* backend_name;\n");
        out.append("} JParserTeaVMCPluginApiHeader;\n");
        out.append("#endif\n\n");
        out.append("#ifndef JPARSER_TEAVMC_ABI_CALLBACK_TYPES_ALREADY_DECLARED\n");
        appendCallbackTypedefs(out, callbackTypedefs);
        out.append("#endif\n");
        for(int i = 0; i < symbols.size(); i++) {
            ExportSymbol symbol = symbols.get(i);
            out.append("typedef ").append(symbol.returnType).append(" (*")
                    .append(typePrefix).append("Fn").append(index(i)).append(")(")
                    .append(symbol.parameters).append(");\n");
        }
        out.append('\n');
        out.append("typedef struct ").append(typePrefix).append("ApiV1 {\n");
        out.append("    JParserTeaVMCPluginApiHeader header;\n");
        for(int i = 0; i < symbols.size(); i++) {
            out.append("    ").append(typePrefix).append("Fn").append(index(i))
                    .append(" fn_").append(index(i)).append(";\n");
        }
        out.append("} ").append(typePrefix).append("ApiV1;\n\n");
        out.append("#endif\n");
        return out.toString();
    }

    private static String dispatchHeader(String identifier, List<ExportSymbol> symbols) {
        String guard = "JPARSER_TEAVMC_" + identifier.toUpperCase(Locale.ROOT) + "_DISPATCH_H";
        String typePrefix = typePrefix(identifier);
        String acquire = acquireName(identifier);
        StringBuilder out = new StringBuilder();
        out.append("#ifndef ").append(guard).append("\n#define ").append(guard).append("\n\n");
        out.append("#include \"teavmc_loader.h\"\n");
        out.append("#include \"TeaVMCAbi.h\"\n\n");
        out.append("#ifdef __cplusplus\nextern \"C\" {\n#endif\n");
        out.append("const ").append(typePrefix).append("ApiV1* ").append(acquire).append("(void);\n");
        out.append("#ifdef __cplusplus\n}\n#endif\n\n");
        for(int i = 0; i < symbols.size(); i++) {
            out.append("#define ").append(symbols.get(i).name).append("(...) ")
                    .append("(").append(acquire).append("()->fn_").append(index(i)).append(")(__VA_ARGS__)\n");
        }
        out.append("\n#endif\n");
        return out.toString();
    }

    private static String dispatchSource(String logicalName, String identifier, List<ExportSymbol> symbols, Fingerprint fingerprint) {
        String typePrefix = typePrefix(identifier);
        String acquire = acquireName(identifier);
        String bind = "jparser_teavmc_" + identifier + "_bind_api_v1";
        String descriptor = "jparser_teavmc_" + identifier + "_descriptor_v1";
        String provider = providerName(identifier);
        StringBuilder out = new StringBuilder();
        out.append("#include \"TeaVMCDispatch.h\"\n");
        out.append("#include <atomic>\n#include <cstdio>\n#include <cstdlib>\n#include <cstring>\n\n");
        out.append("#ifndef JPARSER_TEAVMC_LINKAGE_MODE\n");
        out.append("#define JPARSER_TEAVMC_LINKAGE_MODE JPARSER_TEAVMC_LINKAGE_STATIC\n");
        out.append("#endif\n\n");
        out.append("static std::atomic<const ").append(typePrefix).append("ApiV1*> jparser_teavmc_api(nullptr);\n\n");
        out.append("static int32_t ").append(bind)
                .append("(const JParserTeaVMCPluginApiHeader* header, char* error_message, uint32_t error_message_capacity) {\n");
        out.append("    if(header == nullptr || header->api_size != sizeof(").append(typePrefix).append("ApiV1)) {\n");
        out.append("        if(error_message != nullptr && error_message_capacity > 0) {\n");
        out.append("            std::snprintf(error_message, error_message_capacity, \"Invalid TeaVM C API table size for %s\", \"")
                .append(cString(logicalName)).append("\");\n");
        out.append("        }\n        return JPARSER_TEAVMC_LOADER_PLUGIN_API_INVALID;\n    }\n");
        out.append("    const ").append(typePrefix).append("ApiV1* api = reinterpret_cast<const ")
                .append(typePrefix).append("ApiV1*>(header);\n");
        for(int i = 0; i < symbols.size(); i++) {
            out.append("    if(api->fn_").append(index(i)).append(" == nullptr) {\n");
            out.append("        if(error_message != nullptr && error_message_capacity > 0) {\n");
            out.append("            std::snprintf(error_message, error_message_capacity, \"Missing TeaVM C symbol: %s\", \"")
                    .append(cString(symbols.get(i).name)).append("\");\n");
            out.append("        }\n        return JPARSER_TEAVMC_LOADER_PLUGIN_SYMBOL_MISMATCH;\n    }\n");
        }
        out.append("    jparser_teavmc_api.store(api, std::memory_order_release);\n");
        out.append("    return JPARSER_TEAVMC_LOADER_SUCCESS;\n}\n\n");
        out.append("static const JParserTeaVMCLibraryDescriptor ").append(descriptor).append(" = {\n");
        out.append("    sizeof(JParserTeaVMCLibraryDescriptor),\n");
        out.append("    JPARSER_TEAVMC_DESCRIPTOR_ABI_MAJOR,\n");
        out.append("    JPARSER_TEAVMC_DESCRIPTOR_ABI_MINOR,\n");
        out.append("    JPARSER_TEAVMC_LINKAGE_MODE,\n");
        out.append("    \"").append(cString(logicalName)).append("\",\n");
        out.append("    \"").append(provider).append("\",\n");
        out.append("    JPARSER_TEAVMC_PLUGIN_ABI_MAJOR,\n");
        out.append("    JPARSER_TEAVMC_PLUGIN_ABI_MINOR,\n");
        out.append("    sizeof(").append(typePrefix).append("ApiV1),\n");
        out.append("    ").append(symbols.size()).append("u,\n");
        out.append("    ").append(fingerprint.hiLiteral()).append(",\n");
        out.append("    ").append(fingerprint.loLiteral()).append(",\n");
        out.append("    &").append(bind).append("\n};\n");
        out.append("JPARSER_TEAVMC_REGISTER_LIBRARY_DESCRIPTOR(").append(descriptor).append(");\n\n");
        out.append("extern \"C\" const ").append(typePrefix).append("ApiV1* ").append(acquire).append("(void) {\n");
        out.append("    const ").append(typePrefix).append("ApiV1* api = jparser_teavmc_api.load(std::memory_order_acquire);\n");
        out.append("    if(api == nullptr) {\n");
        out.append("        std::fputs(\"TeaVM C library is not loaded: ").append(cString(logicalName)).append("\\n\", stderr);\n");
        out.append("        std::abort();\n    }\n    return api;\n}\n");
        return out.toString();
    }

    private static String pluginProvider(String logicalName, String identifier, List<ExportSymbol> symbols, Fingerprint fingerprint) {
        String typePrefix = typePrefix(identifier);
        String provider = providerName(identifier);
        StringBuilder out = new StringBuilder();
        out.append("\n/* Generated TeaVM C plugin ABI provider. */\n");
        out.append("#define JPARSER_TEAVMC_ABI_CALLBACK_TYPES_ALREADY_DECLARED 1\n");
        out.append("#include \"../").append(ABI_DIRECTORY).append('/').append(ABI_HEADER).append("\"\n");
        out.append("#undef JPARSER_TEAVMC_ABI_CALLBACK_TYPES_ALREADY_DECLARED\n");
        out.append("#ifndef JPARSER_TEAVMC_BACKEND_NAME\n#define JPARSER_TEAVMC_BACKEND_NAME \"\"\n#endif\n\n");
        out.append("static const ").append(typePrefix).append("ApiV1 jparser_teavmc_plugin_api_v1 = {\n");
        out.append("    { JPARSER_TEAVMC_PLUGIN_API_MAGIC, JPARSER_TEAVMC_PLUGIN_ABI_MAJOR, ")
                .append("JPARSER_TEAVMC_PLUGIN_ABI_MINOR, sizeof(JParserTeaVMCPluginApiHeader), sizeof(")
                .append(typePrefix).append("ApiV1), ").append(symbols.size()).append("u, ")
                .append(fingerprint.hiLiteral()).append(", ").append(fingerprint.loLiteral()).append(", \"")
                .append(cString(logicalName)).append("\", JPARSER_TEAVMC_BACKEND_NAME },\n");
        for(int i = 0; i < symbols.size(); i++) {
            out.append("    &").append(symbols.get(i).name);
            out.append(i + 1 < symbols.size() ? ",\n" : "\n");
        }
        out.append("};\n\n");
        out.append("extern \"C\" TEAVMC_EXPORT const JParserTeaVMCPluginApiHeader* ").append(provider).append("(void) {\n");
        out.append("    return &jparser_teavmc_plugin_api_v1.header;\n}\n");
        return out.toString();
    }

    private static String metadata(String logicalName, String identifier, List<ExportSymbol> symbols, Fingerprint fingerprint) {
        return "logical-name=" + logicalName + "\n"
                + "provider-symbol=" + providerName(identifier) + "\n"
                + "abi-major=1\nabi-minor=0\n"
                + "api-symbol-count=" + symbols.size() + "\n"
                + "fingerprint-hi=" + fingerprint.hiHex() + "\n"
                + "fingerprint-lo=" + fingerprint.loHex() + "\n";
    }

    private static Fingerprint fingerprint(String logicalName, List<String> callbackTypedefs,
                                           List<ExportSymbol> symbols) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(("jparser-teavmc-api-v1\\n" + logicalName + "\\n").getBytes(StandardCharsets.UTF_8));
            for(String callbackTypedef : callbackTypedefs) {
                digest.update(("callback|" + callbackTypedef).getBytes(StandardCharsets.UTF_8));
                digest.update((byte)'\n');
            }
            for(ExportSymbol symbol : symbols) {
                digest.update(symbol.canonicalSignature().getBytes(StandardCharsets.UTF_8));
                digest.update((byte)'\n');
            }
            byte[] hash = digest.digest();
            return new Fingerprint(readLong(hash, 0), readLong(hash, 8));
        }
        catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static long readLong(byte[] bytes, int offset) {
        long value = 0;
        for(int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[offset + i] & 0xFFL);
        }
        return value;
    }

    private static StringBuilder commonHeaderStart(String guard) {
        StringBuilder out = new StringBuilder();
        out.append("#ifndef ").append(guard).append("\n#define ").append(guard).append("\n\n");
        out.append("#include <stdint.h>\n#include <stdbool.h>\n\n");
        return out;
    }

    private static void appendCallbackTypedefs(StringBuilder out, List<String> callbackTypedefs) {
        for(String declaration : callbackTypedefs) {
            out.append(declaration).append('\n');
        }
        if(!callbackTypedefs.isEmpty()) {
            out.append('\n');
        }
    }

    private static String normalizeLogicalName(String libraryName) {
        if(libraryName == null || libraryName.trim().isEmpty()) {
            return "library";
        }
        return libraryName.trim();
    }

    private static String cIdentifier(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        if(normalized.isEmpty()) {
            normalized = "library";
        }
        if(Character.isDigit(normalized.charAt(0))) {
            normalized = "lib_" + normalized;
        }
        return normalized;
    }

    private static String typePrefix(String identifier) {
        return "JParserTeaVMC_" + identifier + "_";
    }

    private static String providerName(String identifier) {
        return "jparser_teavmc_" + identifier + "_get_api_v1";
    }

    private static String acquireName(String identifier) {
        return "jparser_teavmc_" + identifier + "_api_acquire_v1";
    }

    private static String index(int value) {
        return String.format(Locale.ROOT, "%04d", value);
    }

    private static String normalizeWhitespace(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String cString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void write(Path path, String value) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, value.getBytes(StandardCharsets.UTF_8));
    }

    static final class ExportSymbol {
        final String name;
        final String returnType;
        final String parameters;

        ExportSymbol(String name, String returnType, String parameters) {
            this.name = name;
            this.returnType = returnType;
            this.parameters = parameters;
        }

        String prototype() {
            return returnType + " " + name + "(" + parameters + ");";
        }

        String canonicalSignature() {
            return name + "|" + returnType + "(" + canonicalParameters(parameters) + ")";
        }

        private static String canonicalParameters(String parameters) {
            if(parameters.isEmpty() || parameters.equals("void")) {
                return parameters;
            }
            ArrayList<String> values = splitParameters(parameters);
            StringBuilder out = new StringBuilder();
            for(int i = 0; i < values.size(); i++) {
                if(i > 0) {
                    out.append(',');
                }
                out.append(stripParameterName(values.get(i)));
            }
            return out.toString();
        }

        private static ArrayList<String> splitParameters(String parameters) {
            ArrayList<String> result = new ArrayList<>();
            int depth = 0;
            int start = 0;
            for(int i = 0; i < parameters.length(); i++) {
                char c = parameters.charAt(i);
                if(c == '(' || c == '[') depth++;
                else if(c == ')' || c == ']') depth--;
                else if(c == ',' && depth == 0) {
                    result.add(parameters.substring(start, i).trim());
                    start = i + 1;
                }
            }
            result.add(parameters.substring(start).trim());
            return result;
        }

        private static String stripParameterName(String parameter) {
            String normalized = normalizeWhitespace(parameter);
            normalized = normalized.replaceAll("\\(\\s*\\*\\s*[A-Za-z_][A-Za-z0-9_]*\\s*\\)", "(*)");
            Matcher matcher = LAST_IDENTIFIER.matcher(normalized);
            if(matcher.find()) {
                String before = normalized.substring(0, matcher.start()).trim();
                if(!before.isEmpty()) {
                    normalized = before;
                }
            }
            return normalizeWhitespace(normalized);
        }
    }

    static final class Fingerprint {
        final long hi;
        final long lo;

        Fingerprint(long hi, long lo) {
            this.hi = hi;
            this.lo = lo;
        }

        String hiLiteral() {
            return "UINT64_C(0x" + hiHex() + ")";
        }

        String loLiteral() {
            return "UINT64_C(0x" + loHex() + ")";
        }

        String hiHex() {
            return String.format(Locale.ROOT, "%016X", hi);
        }

        String loHex() {
            return String.format(Locale.ROOT, "%016X", lo);
        }
    }
}
