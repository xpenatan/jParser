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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes generated TeaVM C bridge resources in a portable CMake layout.
 *
 * <p>The legacy {@code META-INF/gdx-teavm.properties} discovery marker and
 * {@code TEAVM_APP_TARGET} CMake fallback remain part of the emitted contract so
 * existing gdx-teavm consumers continue to discover and include these resources.</p>
 */
final class TeaVMCPortableResourceWriter {
    private static final String PROPERTIES_PATH = "META-INF/gdx-teavm.properties";
    private static final String RESOURCE_ROOT = "external_cpp/jparser";
    private static final String GENERATED_RESOURCES_PATH = "build/generated/jparser/resources/main";
    private static final String CMAKE_TEMPLATE_RESOURCE =
            "com/github/xpenatan/jParser/builder/tool/TeaVMCPortableResourceWriter.cmake";
    private static final String LIBRARY_NAME_PLACEHOLDER = "@LIBRARY_NAME@";
    private static final String RESOURCE_NAME_PLACEHOLDER = "@RESOURCE_NAME@";
    private static final String VARIABLE_PREFIX_PLACEHOLDER = "@VARIABLE_PREFIX@";
    private static final Pattern TEMPLATE_PLACEHOLDER_PATTERN = Pattern.compile("@[A-Z][A-Z0-9_]*@");

    private TeaVMCPortableResourceWriter() {
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
            throw new RuntimeException("Failed to write portable TeaVM C resources for " + op.libName, e);
        }
    }

    static String generateCMake(String libName) throws IOException {
        String resourceName = resourceName(libName);
        String variablePrefix = "JPARSER_" + cmakeIdentifier(libName) + "_TEAVMC";
        String cmake = readClasspathText(CMAKE_TEMPLATE_RESOURCE);
        cmake = replaceRequiredPlaceholder(cmake, LIBRARY_NAME_PLACEHOLDER, libName);
        cmake = replaceRequiredPlaceholder(cmake, RESOURCE_NAME_PLACEHOLDER, resourceName);
        cmake = replaceRequiredPlaceholder(cmake, VARIABLE_PREFIX_PLACEHOLDER, variablePrefix);

        Matcher unresolvedPlaceholder = TEMPLATE_PLACEHOLDER_PATTERN.matcher(cmake);
        if(unresolvedPlaceholder.find()) {
            throw new IOException("Unresolved CMake template placeholder: " + unresolvedPlaceholder.group());
        }
        return cmake;
    }

    private static String replaceRequiredPlaceholder(String template, String placeholder, String value) throws IOException {
        if(!template.contains(placeholder)) {
            throw new IOException("Missing CMake template placeholder: " + placeholder);
        }
        return template.replace(placeholder, value);
    }

    private static String readClasspathText(String resource) throws IOException {
        try(InputStream input = requireClasspathResource(resource)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
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
        try(InputStream input = requireClasspathResource(resource)) {
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static InputStream requireClasspathResource(String resource) throws IOException {
        InputStream input = TeaVMCPortableResourceWriter.class.getClassLoader().getResourceAsStream(resource);
        if(input == null) {
            throw new IOException("Missing classpath resource: " + resource);
        }
        return input;
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
