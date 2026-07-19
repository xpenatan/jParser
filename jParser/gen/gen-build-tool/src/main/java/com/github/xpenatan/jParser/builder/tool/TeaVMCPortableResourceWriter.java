package com.github.xpenatan.jParser.builder.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private static final String PROPERTIES_CONTENT =
            "ignore-resources=META-INF\nresources=loader-c-\n";
    private static final String RESOURCE_ROOT = "external_cpp/jparser";
    private static final String GENERATED_RESOURCES_PATH = "build/generated/jparser/resources/main";
    private static final String CMAKE_TEMPLATE_RESOURCE =
            "com/github/xpenatan/jParser/builder/tool/TeaVMCPortableResourceWriter.cmake";
    private static final String LIBRARY_NAME_PLACEHOLDER = "@LIBRARY_NAME@";
    private static final String RESOURCE_NAME_PLACEHOLDER = "@RESOURCE_NAME@";
    private static final String VARIABLE_PREFIX_PLACEHOLDER = "@VARIABLE_PREFIX@";
    private static final String DEFAULT_LINKAGE_PLACEHOLDER = "@DEFAULT_LINKAGE@";
    private static final String CONSUMER_CONFIG_PLACEHOLDER = "@CONSUMER_CONFIG@";
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
            Path abiRoot = libRoot.resolve("teavmcabi");
            Files.createDirectories(glueRoot);
            Files.createDirectories(customRoot);
            Files.createDirectories(runtimeRoot);
            Files.createDirectories(sourceHeadersRoot);

            Path generatedGlueRoot = Path.of(op.getCPPDestinationPath(), "teavmcglue");
            Path generatedAbiRoot = Path.of(op.getCPPDestinationPath(), "teavmcabi");
            copyRequired(generatedGlueRoot.resolve("TeaVMCGlue.cpp"), glueRoot.resolve("TeaVMCGlue.cpp"));
            copyRequired(generatedGlueRoot.resolve("TeaVMCGlue.h"), glueRoot.resolve("TeaVMCGlue.h"));
            copyRequired(generatedAbiRoot.resolve("TeaVMCAbi.h"), abiRoot.resolve("TeaVMCAbi.h"));
            copyRequired(generatedAbiRoot.resolve("TeaVMCDispatch.h"), abiRoot.resolve("TeaVMCDispatch.h"));
            copyRequired(generatedAbiRoot.resolve("TeaVMCDispatch.cpp"), abiRoot.resolve("TeaVMCDispatch.cpp"));
            copyRequired(generatedAbiRoot.resolve("teavmc_abi.properties"), abiRoot.resolve("teavmc_abi.properties"));
            copyCustomSources(Path.of(op.getCustomSourceDir()), customRoot);
            copySourceHeaders(op.getSourceDir(), sourceHeadersRoot);
            copyRuntimeHelper(runtimeRoot);

            // loader-c intentionally has no discovery marker of its own: that
            // would collide when Android merges runtime-c's transitive jars.
            // Every generated binding marker selects the versioned loader-c jar
            // as an additional portable-resource input instead.
            writeString(resourceRoot.resolve(PROPERTIES_PATH), generateProperties());
            copyRequired(generatedAbiRoot.resolve("teavmc_imports.h"),
                    libRoot.resolve("imports").resolve("teavmc_imports.h"));
            writeString(resourceRoot.resolve("external_cpp/cmake/post_target")
                    .resolve("jparser_" + resourceName(op.libName) + "_teavm_c.cmake"),
                    generateCMake(op.libName, op.teaVMCLinkage, op.teaVMCConsumers));
        }
        catch(IOException e) {
            throw new RuntimeException("Failed to write portable TeaVM C resources for " + op.libName, e);
        }
    }

    static String generateCMake(String libName) throws IOException {
        return generateCMake(libName, TeaVMCLinkage.STATIC, new ArrayList<>());
    }

    static String generateProperties() {
        return PROPERTIES_CONTENT;
    }

    static String generateCMake(String libName, TeaVMCLinkage linkage) throws IOException {
        return generateCMake(libName, linkage, new ArrayList<>());
    }

    static String generateCMake(
            String libName,
            TeaVMCLinkage linkage,
            List<TeaVMCConsumerConfig> consumers) throws IOException {
        String resourceName = resourceName(libName);
        String variablePrefix = "JPARSER_" + cmakeIdentifier(libName) + "_TEAVMC";
        String cmake = readClasspathText(CMAKE_TEMPLATE_RESOURCE);
        cmake = replaceRequiredPlaceholder(cmake, LIBRARY_NAME_PLACEHOLDER, libName);
        cmake = replaceRequiredPlaceholder(cmake, RESOURCE_NAME_PLACEHOLDER, resourceName);
        cmake = replaceRequiredPlaceholder(cmake, VARIABLE_PREFIX_PLACEHOLDER, variablePrefix);
        TeaVMCLinkage effectiveLinkage = linkage != null ? linkage : TeaVMCLinkage.STATIC;
        cmake = replaceRequiredPlaceholder(cmake, DEFAULT_LINKAGE_PLACEHOLDER, effectiveLinkage.name());
        cmake = replaceRequiredPlaceholder(
                cmake,
                CONSUMER_CONFIG_PLACEHOLDER,
                generateConsumerCMake(libName, variablePrefix, consumers));

        Matcher unresolvedPlaceholder = TEMPLATE_PLACEHOLDER_PATTERN.matcher(cmake);
        if(unresolvedPlaceholder.find()) {
            throw new IOException("Unresolved CMake template placeholder: " + unresolvedPlaceholder.group());
        }
        return cmake;
    }

    private static String generateConsumerCMake(
            String libName,
            String variablePrefix,
            List<TeaVMCConsumerConfig> consumers) throws IOException {
        if(consumers == null || consumers.isEmpty()) {
            return "# No additional TeaVM C consumer native requirements were declared.";
        }

        ArrayList<TeaVMCConsumerConfig> sorted = new ArrayList<>(consumers);
        sorted.sort(Comparator
                .comparing((TeaVMCConsumerConfig value) -> required(value.targetName, "targetName"))
                .thenComparing(value -> required(value.variantName, "variantName")));
        validateConsumers(sorted);

        String matchCount = variablePrefix + "_CONSUMER_MATCH_COUNT";
        String availableCount = variablePrefix + "_CONSUMER_AVAILABLE_COUNT";
        String selected = variablePrefix + "_CONSUMER_SELECTED";
        String variant = variablePrefix + "_CONSUMER_VARIANT";
        String root = "${" + variablePrefix + "_NATIVE_ROOT}/${" + variablePrefix + "_PLATFORM}";
        StringBuilder out = new StringBuilder();
        out.append("# Additional native requirements generated from the producer's TeaVM C consumer declarations.\n");
        out.append("set(").append(matchCount).append(" 0)\n");
        out.append("set(").append(availableCount).append(" 0)\n");
        out.append("set(").append(selected).append(" \"\")\n");
        out.append("set(").append(variant).append(" \"\")\n");

        for(int i = 0; i < sorted.size(); i++) {
            TeaVMCConsumerConfig consumer = sorted.get(i);
            String key = consumerKey(consumer);
            String candidateMatch = variablePrefix + "_CONSUMER_CANDIDATE_" + i + "_MATCH";
            out.append("if(").append(platformCondition(variablePrefix, consumer.targetName)).append(")\n");
            out.append("  math(EXPR ").append(availableCount).append(" \"${")
                    .append(availableCount).append("} + 1\")\n");
            out.append("  set(").append(candidateMatch).append(" TRUE)\n");
            for(String selector : consumer.selectorResources) {
                out.append("  if(NOT EXISTS \"").append(root).append("/")
                        .append(cmakeEscape(selector)).append("\")\n");
                out.append("    set(").append(candidateMatch).append(" FALSE)\n");
                out.append("  endif()\n");
            }
            out.append("  if(").append(candidateMatch).append(")\n");
            out.append("    math(EXPR ").append(matchCount).append(" \"${")
                    .append(matchCount).append("} + 1\")\n");
            out.append("    set(").append(selected).append(" \"")
                    .append(cmakeEscape(key)).append("\")\n");
            out.append("  endif()\n");
            out.append("endif()\n");
        }

        out.append("if(").append(matchCount).append(" GREATER 1)\n");
        out.append("  message(FATAL_ERROR \"Multiple packaged ").append(cmakeEscape(libName))
                .append(" TeaVM C consumer variants match platform ${")
                .append(variablePrefix).append("_PLATFORM}. Add exactly one matching native artifact.\")\n");
        out.append("endif()\n");
        out.append("if(").append(availableCount).append(" GREATER 0 AND ")
                .append(matchCount).append(" EQUAL 0)\n");
        out.append("  message(FATAL_ERROR \"No packaged ").append(cmakeEscape(libName))
                .append(" TeaVM C consumer variant matches platform ${")
                .append(variablePrefix).append("_PLATFORM}. Add the matching native artifact.\")\n");
        out.append("endif()\n");

        for(int i = 0; i < sorted.size(); i++) {
            TeaVMCConsumerConfig consumer = sorted.get(i);
            String key = consumerKey(consumer);
            out.append(i == 0 ? "if(" : "elseif(")
                    .append(selected).append(" STREQUAL \"")
                    .append(cmakeEscape(key)).append("\")\n");
            out.append("  set(").append(variant).append(" \"")
                    .append(cmakeEscape(consumer.variantName)).append("\")\n");
            appendConsumerBody(out, libName, variablePrefix, root, consumer, i);
        }
        out.append("endif()\n");
        return out.toString().trim();
    }

    private static void appendConsumerBody(
            StringBuilder out,
            String libName,
            String variablePrefix,
            String root,
            TeaVMCConsumerConfig consumer,
            int consumerIndex) {
        for(int i = 0; i < consumer.headerDirs.size(); i++) {
            String headerDir = consumer.headerDirs.get(i);
            String headerVariable = variablePrefix + "_CONSUMER_" + consumerIndex + "_HEADER_" + i;
            out.append("  set(").append(headerVariable).append(" \"")
                    .append(root).append("/").append(cmakeEscape(headerDir)).append("\")\n");
            out.append("  if(NOT IS_DIRECTORY \"${").append(headerVariable).append("}\")\n");
            out.append("    message(FATAL_ERROR \"Missing packaged ").append(cmakeEscape(libName))
                    .append(" consumer header directory: ${").append(headerVariable).append("}\")\n");
            out.append("  endif()\n");
            out.append("  target_include_directories(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"${")
                    .append(headerVariable).append("}\")\n");
        }
        appendTargetValues(out, "target_compile_definitions", consumer.compileDefinitions, 2);
        appendTargetValues(out, "target_compile_options", consumer.compileOptions, 2);

        if(consumer.staticLibraries.isEmpty()
                && consumer.staticLinkLibraries.isEmpty()
                && consumer.staticLinkOptions.isEmpty()) {
            return;
        }

        out.append("  if(").append(variablePrefix).append("_LINKAGE STREQUAL \"STATIC\")\n");
        for(int i = 0; i < consumer.staticLibraries.size(); i++) {
            TeaVMCConsumerConfig.StaticLibrary library = consumer.staticLibraries.get(i);
            String libraryVariable = variablePrefix + "_CONSUMER_" + consumerIndex + "_STATIC_LIBRARY_" + i;
            String candidatesVariable = libraryVariable + "_CANDIDATES";
            out.append("    set(").append(libraryVariable).append(" \"\")\n");
            if(library.overrideVariable != null && !library.overrideVariable.trim().isEmpty()) {
                String override = library.overrideVariable.trim();
                out.append("    if(DEFINED ").append(override).append(" AND NOT \"${")
                        .append(override).append("}\" STREQUAL \"\")\n");
                out.append("      set(").append(libraryVariable).append(" \"${")
                        .append(override).append("}\")\n");
                out.append("    endif()\n");
            }
            out.append("    if(\"${").append(libraryVariable).append("}\" STREQUAL \"\")\n");
            out.append("      set(").append(candidatesVariable).append(")\n");
            out.append("      if(WIN32 AND NOT \"${").append(variablePrefix)
                    .append("_MSVC_RUNTIME_SUBDIR}\" STREQUAL \"\")\n");
            out.append("        list(APPEND ").append(candidatesVariable).append(" \"")
                    .append(root).append("/${").append(variablePrefix).append("_MSVC_RUNTIME_SUBDIR}/")
                    .append(cmakeEscape(library.resourcePath)).append("\")\n");
            out.append("      endif()\n");
            out.append("      list(APPEND ").append(candidatesVariable).append(" \"")
                    .append(root).append("/").append(cmakeEscape(library.resourcePath)).append("\")\n");
            out.append("      foreach(").append(libraryVariable).append("_CANDIDATE IN LISTS ")
                    .append(candidatesVariable).append(")\n");
            out.append("        if(EXISTS \"${").append(libraryVariable).append("_CANDIDATE}\")\n");
            out.append("          set(").append(libraryVariable).append(" \"${")
                    .append(libraryVariable).append("_CANDIDATE}\")\n");
            out.append("          break()\n");
            out.append("        endif()\n");
            out.append("      endforeach()\n");
            out.append("    endif()\n");
            out.append("    if(NOT EXISTS \"${").append(libraryVariable).append("}\")\n");
            out.append("      message(FATAL_ERROR \"Missing packaged ").append(cmakeEscape(libName))
                    .append(" static consumer library for variant ").append(cmakeEscape(consumer.variantName))
                    .append(": ").append(cmakeEscape(library.resourcePath)).append("\")\n");
            out.append("    endif()\n");
            out.append("    target_link_libraries(${JPARSER_TEAVMC_APP_TARGET} PRIVATE \"${")
                    .append(libraryVariable).append("}\")\n");
        }
        appendTargetValues(out, "target_link_libraries", consumer.staticLinkLibraries, 4);
        appendTargetValues(out, "target_link_options", consumer.staticLinkOptions, 4);
        out.append("  endif()\n");
    }

    private static void appendTargetValues(
            StringBuilder out,
            String command,
            List<String> values,
            int indent) {
        if(values.isEmpty()) {
            return;
        }
        String spaces = " ".repeat(indent);
        out.append(spaces).append(command).append("(${JPARSER_TEAVMC_APP_TARGET} PRIVATE");
        for(String value : values) {
            out.append(" \"").append(cmakeEscape(value)).append("\"");
        }
        out.append(")\n");
    }

    private static void validateConsumers(List<TeaVMCConsumerConfig> consumers) throws IOException {
        Set<String> keys = new HashSet<>();
        for(TeaVMCConsumerConfig consumer : consumers) {
            String targetName = required(consumer.targetName, "targetName");
            requiredPlatform(targetName);
            required(consumer.variantName, "variantName");
            String key = consumerKey(consumer);
            if(!keys.add(key)) {
                throw new IOException("Duplicate TeaVM C consumer declaration: " + key);
            }
            for(String value : consumer.selectorResources) {
                validateResourcePath(value);
            }
            for(String value : consumer.headerDirs) {
                validateResourcePath(value);
            }
            for(TeaVMCConsumerConfig.StaticLibrary library : consumer.staticLibraries) {
                if(library == null) {
                    throw new IOException("TeaVM C consumer static library must not be null");
                }
                validateResourcePath(library.resourcePath);
                if(library.overrideVariable != null && !library.overrideVariable.trim().isEmpty()
                        && !library.overrideVariable.trim().matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    throw new IOException("Invalid TeaVM C consumer override variable: " + library.overrideVariable);
                }
            }
        }
    }

    private static String platformCondition(String variablePrefix, String targetName) throws IOException {
        String platform = requiredPlatform(targetName);
        if(platform.endsWith("/*")) {
            return "\"${" + variablePrefix + "_PLATFORM}\" MATCHES \"^"
                    + cmakeEscape(platform.substring(0, platform.length() - 1)) + "\"";
        }
        return "\"${" + variablePrefix + "_PLATFORM}\" STREQUAL \"" + cmakeEscape(platform) + "\"";
    }

    private static String requiredPlatform(String targetName) throws IOException {
        switch(targetName) {
            case "windows64_teavm_c":
                return "windows_x64";
            case "linux64_teavm_c":
                return "linux_x64";
            case "mac64_teavm_c":
                return "mac_x64";
            case "macArm_teavm_c":
                return "mac_arm64";
            case "android_teavm_c":
                return "android/*";
            default:
                throw new IOException("Unsupported TeaVM C consumer target: " + targetName);
        }
    }

    private static String consumerKey(TeaVMCConsumerConfig consumer) {
        return consumer.targetName + ":" + consumer.variantName;
    }

    private static String required(String value, String name) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TeaVM C consumer " + name + " must not be blank");
        }
        return value.trim();
    }

    private static void validateResourcePath(String value) throws IOException {
        if(value == null || value.trim().isEmpty()) {
            throw new IOException("TeaVM C consumer resource path must not be blank");
        }
        String normalized = value.replace('\\', '/');
        if(normalized.startsWith("/")
                || normalized.matches("^[A-Za-z]:/.*")
                || normalized.equals("..")
                || normalized.startsWith("../")
                || normalized.contains("/../")) {
            throw new IOException("TeaVM C consumer resource path must be relative: " + value);
        }
    }

    private static String cmakeEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace(";", "\\;");
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
