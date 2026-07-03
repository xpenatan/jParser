package com.github.xpenatan.jParser.builder.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TeaVMCGdxTeaVMResourceWriter {
    private static final String PROPERTIES_PATH = "META-INF/gdx-teavm.properties";
    private static final String RESOURCE_ROOT = "external_cpp/jparser";
    private static final String GENERATED_RESOURCES_PATH = "build/generated/jparser/resources/main";

    private TeaVMCGdxTeaVMResourceWriter() {
    }

    static void write(BuildToolOptions op) {
        try {
            Path resourceRoot = Path.of(op.getModuleCPath(), GENERATED_RESOURCES_PATH);
            Path libRoot = resourceRoot.resolve(RESOURCE_ROOT).resolve(resourceName(op.libName));
            cleanDirectory(libRoot);

            Path glueRoot = libRoot.resolve("glue");
            Path customRoot = libRoot.resolve("custom");
            Path runtimeRoot = libRoot.resolve("runtime");
            Path sourceHeadersRoot = libRoot.resolve("source");
            Files.createDirectories(glueRoot);
            Files.createDirectories(customRoot);
            Files.createDirectories(runtimeRoot);
            Files.createDirectories(sourceHeadersRoot);

            Path generatedGlueRoot = Path.of(op.getCPPDestinationPath(), "teavmcglue");
            copyRequired(generatedGlueRoot.resolve("TeaVMCGlue.cpp"), glueRoot.resolve("TeaVMCGlue.cpp"));
            copyRequired(generatedGlueRoot.resolve("TeaVMCGlue.h"), glueRoot.resolve("TeaVMCGlue.h"));
            copyCustomSources(Path.of(op.getCustomSourceDir()), customRoot);
            copySourceHeaders(op.getSourceDir(), sourceHeadersRoot);
            copyRuntimeHelper(runtimeRoot);

            writeString(resourceRoot.resolve(PROPERTIES_PATH), "ignore-resources=META-INF\n");
            writeString(libRoot.resolve("imports").resolve("teavmc_imports.h"),
                    generateImportHeader(generatedGlueRoot.resolve("TeaVMCGlue.h")));
            writeString(resourceRoot.resolve("external_cpp/cmake/post_target")
                    .resolve("jparser_" + resourceName(op.libName) + "_teavm_c.cmake"), generateCMake(op.libName));
        }
        catch(IOException e) {
            throw new RuntimeException("Failed to write gdx-teavm TeaVM C resources for " + op.libName, e);
        }
    }

    private static String generateCMake(String libName) {
        String resourceName = resourceName(libName);
        String variablePrefix = "JPARSER_" + cmakeIdentifier(libName) + "_TEAVMC";
        StringBuilder sb = new StringBuilder();
        sb.append("set(").append(variablePrefix).append("_ROOT \"${CMAKE_CURRENT_SOURCE_DIR}/c/external_cpp/jparser/")
                .append(resourceName).append("\")\n");
        sb.append("set(").append(variablePrefix).append("_GLUE_ROOT \"${").append(variablePrefix)
                .append("_ROOT}/glue\")\n");
        sb.append("set(").append(variablePrefix).append("_CUSTOM_ROOT \"${").append(variablePrefix)
                .append("_ROOT}/custom\")\n");
        sb.append("set(").append(variablePrefix).append("_RUNTIME_ROOT \"${").append(variablePrefix)
                .append("_ROOT}/runtime\")\n");
        sb.append("set(").append(variablePrefix).append("_SOURCE_ROOT \"${").append(variablePrefix)
                .append("_ROOT}/source\")\n");
        sb.append("set(").append(variablePrefix).append("_IMPORT_HEADER \"${").append(variablePrefix)
                .append("_ROOT}/imports/teavmc_imports.h\")\n");
        sb.append("set(").append(variablePrefix).append("_NATIVE_ROOT \"${").append(variablePrefix)
                .append("_ROOT}/native\")\n\n");

        sb.append("set(").append(variablePrefix).append("_LIBRARY_CANDIDATES)\n");
        sb.append("if(WIN32)\n");
        sb.append("  list(APPEND ").append(variablePrefix).append("_LIBRARY_CANDIDATES\n");
        sb.append("    \"${").append(variablePrefix).append("_NATIVE_ROOT}/windows_x64/")
                .append(libName).append("64_.lib\")\n");
        sb.append("elseif(APPLE)\n");
        sb.append("  if(CMAKE_SYSTEM_PROCESSOR MATCHES \"arm64|aarch64|ARM64\")\n");
        sb.append("    list(APPEND ").append(variablePrefix).append("_LIBRARY_CANDIDATES\n");
        sb.append("      \"${").append(variablePrefix).append("_NATIVE_ROOT}/mac_arm64/lib")
                .append(libName).append("arm64_.a\")\n");
        sb.append("  else()\n");
        sb.append("    list(APPEND ").append(variablePrefix).append("_LIBRARY_CANDIDATES\n");
        sb.append("      \"${").append(variablePrefix).append("_NATIVE_ROOT}/mac_x64/lib")
                .append(libName).append("64_.a\")\n");
        sb.append("  endif()\n");
        sb.append("elseif(UNIX)\n");
        sb.append("  list(APPEND ").append(variablePrefix).append("_LIBRARY_CANDIDATES\n");
        sb.append("    \"${").append(variablePrefix).append("_NATIVE_ROOT}/linux_x64/lib")
                .append(libName).append("64_.a\")\n");
        sb.append("endif()\n\n");

        sb.append("foreach(").append(variablePrefix).append("_LIBRARY_CANDIDATE IN LISTS ")
                .append(variablePrefix).append("_LIBRARY_CANDIDATES)\n");
        sb.append("  if(EXISTS \"${").append(variablePrefix).append("_LIBRARY_CANDIDATE}\")\n");
        sb.append("    set(").append(variablePrefix).append("_LIBRARY \"${").append(variablePrefix)
                .append("_LIBRARY_CANDIDATE}\")\n");
        sb.append("    break()\n");
        sb.append("  endif()\n");
        sb.append("endforeach()\n\n");

        sb.append("if(NOT ").append(variablePrefix).append("_LIBRARY)\n");
        sb.append("  message(FATAL_ERROR \"Missing ").append(libName)
                .append(" TeaVM C static library under ${").append(variablePrefix)
                .append("_NATIVE_ROOT}. Add the matching native TeaVM C artifact to the app classpath or build/package it first.\")\n");
        sb.append("endif()\n\n");

        sb.append("set(").append(variablePrefix).append("_GLUE_SOURCE \"${").append(variablePrefix)
                .append("_GLUE_ROOT}/TeaVMCGlue.cpp\")\n");
        sb.append("if(NOT EXISTS \"${").append(variablePrefix).append("_GLUE_SOURCE}\")\n");
        sb.append("  message(FATAL_ERROR \"Missing ").append(libName).append(" TeaVM C glue source: ${")
                .append(variablePrefix).append("_GLUE_SOURCE}\")\n");
        sb.append("endif()\n");
        sb.append("if(NOT EXISTS \"${").append(variablePrefix).append("_IMPORT_HEADER}\")\n");
        sb.append("  message(FATAL_ERROR \"Missing ").append(libName).append(" TeaVM C import header: ${")
                .append(variablePrefix).append("_IMPORT_HEADER}\")\n");
        sb.append("endif()\n\n");

        sb.append("enable_language(CXX)\n");
        sb.append("target_sources(${TEAVM_APP_TARGET} PRIVATE \"${").append(variablePrefix)
                .append("_GLUE_SOURCE}\")\n");
        sb.append("set_source_files_properties(\"${").append(variablePrefix)
                .append("_GLUE_SOURCE}\" PROPERTIES LANGUAGE CXX)\n");
        sb.append("set_property(TARGET ${TEAVM_APP_TARGET} PROPERTY CXX_STANDARD 17)\n");
        sb.append("set_property(TARGET ${TEAVM_APP_TARGET} PROPERTY CXX_STANDARD_REQUIRED ON)\n\n");

        sb.append("target_include_directories(${TEAVM_APP_TARGET} PRIVATE\n");
        sb.append("  \"${").append(variablePrefix).append("_GLUE_ROOT}\"\n");
        sb.append("  \"${").append(variablePrefix).append("_CUSTOM_ROOT}\"\n");
        sb.append("  \"${").append(variablePrefix).append("_RUNTIME_ROOT}\"\n");
        sb.append("  \"${").append(variablePrefix).append("_SOURCE_ROOT}\"\n");
        sb.append("  \"${").append(variablePrefix).append("_SOURCE_ROOT}/include\"\n");
        sb.append("  \"${").append(variablePrefix).append("_SOURCE_ROOT}/src\")\n\n");

        sb.append("set(").append(variablePrefix)
                .append("_PROJECT_ROOT \"${CMAKE_CURRENT_SOURCE_DIR}/c\")\n");
        sb.append("file(GLOB_RECURSE ").append(variablePrefix).append("_GENERATED_C_SOURCES CONFIGURE_DEPENDS\n");
        sb.append("  \"${").append(variablePrefix).append("_PROJECT_ROOT}/src/*.c\")\n");
        sb.append("\n");
        sb.append("if(MSVC)\n");
        sb.append("  foreach(").append(variablePrefix).append("_SOURCE IN LISTS ")
                .append(variablePrefix).append("_GENERATED_C_SOURCES)\n");
        sb.append("    set_source_files_properties(\"${").append(variablePrefix)
                .append("_SOURCE}\" PROPERTIES COMPILE_OPTIONS \"/FI${")
                .append(variablePrefix).append("_IMPORT_HEADER}\")\n");
        sb.append("  endforeach()\n");
        sb.append("else()\n");
        sb.append("  foreach(").append(variablePrefix).append("_SOURCE IN LISTS ")
                .append(variablePrefix).append("_GENERATED_C_SOURCES)\n");
        sb.append("    set_source_files_properties(\"${").append(variablePrefix)
                .append("_SOURCE}\" PROPERTIES COMPILE_OPTIONS \"-include;${")
                .append(variablePrefix).append("_IMPORT_HEADER}\")\n");
        sb.append("  endforeach()\n");
        sb.append("endif()\n\n");

        sb.append("target_link_libraries(${TEAVM_APP_TARGET} PRIVATE \"${")
                .append(variablePrefix).append("_LIBRARY}\")\n");
        return sb.toString();
    }

    private static String generateImportHeader(Path glueHeader) throws IOException {
        List<String> typedefs = new ArrayList<>();
        List<String> prototypes = new ArrayList<>();
        for(String rawLine : Files.readAllLines(glueHeader, StandardCharsets.UTF_8)) {
            String line = rawLine.trim();
            if(line.startsWith("typedef ") && line.contains("fp_")) {
                typedefs.add(line);
            }
            else if(line.startsWith("TEAVMC_EXPORT ")) {
                String prototype = line.substring("TEAVMC_EXPORT ".length()).trim();
                if(prototype.endsWith("{")) {
                    prototype = prototype.substring(0, prototype.length() - 1).trim();
                }
                if(!prototype.endsWith(";")) {
                    prototype += ";";
                }
                prototypes.add(prototype);
            }
        }
        if(prototypes.isEmpty()) {
            throw new IOException("No TeaVM C import prototypes found in " + glueHeader);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#pragma once\n");
        sb.append("#include <stdint.h>\n");
        sb.append("#include <stdbool.h>\n\n");
        sb.append("#ifdef __cplusplus\nextern \"C\" {\n#endif\n\n");
        for(String typedef : typedefs) {
            sb.append(typedef).append('\n');
        }
        if(!typedefs.isEmpty()) {
            sb.append('\n');
        }
        for(String prototype : prototypes) {
            sb.append(prototype).append('\n');
        }
        sb.append("\n#ifdef __cplusplus\n}\n#endif\n");
        return sb.toString();
    }

    private static void copyCustomSources(Path source, Path target) throws IOException {
        if(!Files.exists(source)) {
            return;
        }
        copyTree(source, target);
    }

    private static void copySourceHeaders(String sourceDir, Path target) throws IOException {
        if(sourceDir == null || sourceDir.trim().isEmpty()) {
            return;
        }
        Path source = Path.of(sourceDir);
        if(!Files.exists(source)) {
            return;
        }
        List<Path> conventionalHeaderDirs = conventionalHeaderDirs(source);
        if(!conventionalHeaderDirs.isEmpty()) {
            for(Path headerDir : conventionalHeaderDirs) {
                copyHeaderTree(headerDir, target.resolve(source.relativize(headerDir)));
            }
            return;
        }
        copyHeaderTree(source, target);
    }

    private static List<Path> conventionalHeaderDirs(Path source) {
        List<Path> dirs = new ArrayList<>();
        Path includeDir = source.resolve("include");
        if(Files.isDirectory(includeDir)) {
            dirs.add(includeDir);
        }
        Path srcDir = source.resolve("src");
        if(Files.isDirectory(srcDir)) {
            dirs.add(srcDir);
        }
        return dirs;
    }

    private static void copyRuntimeHelper(Path runtimeRoot) throws IOException {
        copyClasspathResource("RuntimeHelper.h", runtimeRoot.resolve("RuntimeHelper.h"));
    }

    private static void copyClasspathResource(String resource, Path target) throws IOException {
        try(InputStream input = TeaVMCGdxTeaVMResourceWriter.class.getClassLoader().getResourceAsStream(resource)) {
            if(input == null) {
                throw new IOException("Missing classpath resource: " + resource);
            }
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyHeaderTree(Path source, Path target) throws IOException {
        if(Files.isDirectory(source)) {
            Files.createDirectories(target);
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for(Path child : stream) {
                    copyHeaderTree(child, target.resolve(child.getFileName()));
                }
            }
        }
        else if(Files.isRegularFile(source) && isHeaderFile(source)) {
            copyRequired(source, target);
        }
    }

    private static boolean isHeaderFile(Path source) {
        String name = source.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".h") || name.endsWith(".hpp") || name.endsWith(".hh") || name.endsWith(".inl");
    }

    private static void copyRequired(Path source, Path target) throws IOException {
        if(!Files.isRegularFile(source)) {
            throw new IOException("Missing required file: " + source);
        }
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyTree(Path source, Path target) throws IOException {
        if(Files.isDirectory(source)) {
            Files.createDirectories(target);
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for(Path child : stream) {
                    copyTree(child, target.resolve(child.getFileName()));
                }
            }
        }
        else if(Files.isRegularFile(source)) {
            copyRequired(source, target);
        }
    }

    private static void cleanDirectory(Path path) throws IOException {
        if(!Files.exists(path)) {
            return;
        }
        if(Files.isDirectory(path)) {
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for(Path child : stream) {
                    cleanDirectory(child);
                }
            }
        }
        Files.delete(path);
    }

    private static void writeString(Path target, String value) throws IOException {
        Files.createDirectories(target.getParent());
        Files.writeString(target, value, StandardCharsets.UTF_8);
    }

    private static String resourceName(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return normalized.isEmpty() ? "library" : normalized;
    }

    private static String cmakeIdentifier(String value) {
        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
        return normalized.isEmpty() ? "LIBRARY" : normalized;
    }
}
