package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import com.github.xpenatan.jParser.builder.targets.SourceLanguage;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.util.ResourceList;
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLClassOrEnum;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.IDLRenaming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class JParserBuildRunner {

    private static final ThreadLocal<Properties> REQUEST_PROPERTIES = new ThreadLocal<>();

    public static void main(String[] args) {
        build(fromSystemProperties(), args);
    }

    /**
     * Builds from the implementation-neutral request protocol used by the
     * Gradle plugin's isolated generator classpath.
     */
    public static void build(
            Properties properties,
            UnaryOperator<String> methodRenaming,
            UnaryOperator<String> enumRenaming,
            BiFunction<Map<String, String>, String, String> packageRenaming,
            String... args) {
        JParserBuildRequest request = fromProperties(properties);
        if(methodRenaming != null || enumRenaming != null || packageRenaming != null) {
            request.idlRenaming = new IDLRenaming() {
                @Override
                public String obtainNewPackage(IDLClassOrEnum idlClassOrEnum, String classPackage) {
                    if(packageRenaming == null) {
                        return classPackage;
                    }
                    Map<String, String> type = new HashMap<>();
                    type.put("name", idlClassOrEnum.name);
                    if(idlClassOrEnum.subPackage != null) {
                        type.put("subPackage", idlClassOrEnum.subPackage);
                    }
                    type.put("isClass", Boolean.toString(idlClassOrEnum.isClass()));
                    type.put("isEnum", Boolean.toString(idlClassOrEnum.isEnum()));
                    return packageRenaming.apply(type, classPackage);
                }

                @Override
                public String getIDLMethodName(String methodName) {
                    return methodRenaming == null ? methodName : methodRenaming.apply(methodName);
                }

                @Override
                public String getIDLEnumName(String enumName) {
                    return enumRenaming == null ? enumName : enumRenaming.apply(enumName);
                }
            };
        }
        build(request, args);
    }

    public static void build(JParserBuildRequest request, String... args) {
        validate(request);
        boolean previousRuntimeHelperMode = JParser.CREATE_RUNTIME_HELPER;
        try {
            ResourceList.setAdditionalClassPaths(request.additionalJavaClassPaths);
            JParser.CREATE_RUNTIME_HELPER = request.targetConfig.runtimeHelperMode;
            WindowsMSVCTarget.DEBUG_BUILD = request.targetConfig.windowsDebugBuild;
            BuildToolOptions op = new BuildToolOptions(request.params, args);
            op.generateCore = request.generateCore;
            op.keepGeneratedCommandComments = request.keepGeneratedCommandComments;

            if(request.jniSymbolNameMode != null) {
                op.jniClassData.symbolNameMode = toJNISymbolNameMode(request.jniSymbolNameMode);
            }
            if(request.ffmSymbolNameMode != null) {
                op.ffmClassData.symbolNameMode = toFFMSymbolNameMode(request.ffmSymbolNameMode);
            }
            if(request.teaVMCSymbolNameMode != null) {
                op.teaVMCClassData.symbolNameMode = toFFMSymbolNameMode(request.teaVMCSymbolNameMode);
            }
            op.ffmClassData.logMethod = request.ffmLogMethod;
            op.ffmClassData.defaultCritical = request.ffmDefaultCritical;
            op.teaVMCConsumers.addAll(request.teaVMCConsumers);

            for(int i = 0; i < request.additionalIDLPaths.size(); i++) {
                op.addAdditionalIDLPath(IDLReader.parseFile(request.additionalIDLPaths.get(i)));
            }
            for(int i = 0; i < request.additionalIDLRefPaths.size(); i++) {
                op.addAdditionalIDLRefPath(IDLReader.parseFile(request.additionalIDLRefPaths.get(i)));
            }
            if(request.targetConfig.addRuntimeHelperIDL && !request.targetConfig.runtimeHelperMode) {
                op.addAdditionalIDLRefPath(IDLReader.getRuntimeHelperFile());
            }
            for(int i = 0; i < request.additionalSourceDirs.size(); i++) {
                op.addAdditionalSourceDirs(request.additionalSourceDirs.get(i));
            }
            for(int i = 0; i < request.additionalJavaImportPackages.size(); i++) {
                op.addAdditionalJavaImportPackage(request.additionalJavaImportPackages.get(i));
            }

            DefaultBuildTargetFactory factory = new DefaultBuildTargetFactory();
            BuilderTool.build(op, new BuildToolListener() {
                @Override
                public void onAddTarget(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
                    factory.addTargets(op, idlReader, targets, request.targetConfig);
                }
            }, request.idlRenaming);
        }
        finally {
            ResourceList.clearAdditionalClassPaths();
            JParser.CREATE_RUNTIME_HELPER = previousRuntimeHelperMode;
        }
    }

    static JParserBuildRequest fromSystemProperties() {
        return fromProperties(System.getProperties());
    }

    public static JParserBuildRequest fromProperties(Properties properties) {
        if(properties == null) {
            throw new IllegalArgumentException("jParser request properties must not be null");
        }
        REQUEST_PROPERTIES.set(properties);
        try {
            return fromConfiguredProperties();
        }
        finally {
            REQUEST_PROPERTIES.remove();
        }
    }

    private static JParserBuildRequest fromConfiguredProperties() {
        JParserBuildRequest request = new JParserBuildRequest();
        request.generateCore = booleanProperty("jparser.generateCore", request.generateCore);
        request.params.libName = property("jparser.libName", null);
        request.params.idlName = propertyAllowBlank("jparser.idlName", request.params.libName);
        if(request.params.idlName != null && request.params.idlName.trim().isEmpty()) {
            request.params.idlName = null;
        }
        request.params.webModuleName = property("jparser.webModuleName", request.params.libName);
        request.params.packageName = property("jparser.packageName", null);
        request.params.modulePrefix = propertyAllowBlank("jparser.modulePrefix", null);
        request.params.cppSourcePath = property("jparser.cppSourcePath", null);
        request.params.modulePath = property("jparser.modulePath", null);
        request.params.moduleBaseSuffix = property("jparser.moduleBaseSuffix", null);
        request.params.moduleBuildSuffix = property("jparser.moduleBuildSuffix", null);
        request.params.moduleCoreSuffix = property("jparser.moduleCoreSuffix", null);
        request.params.moduleJNISuffix = property("jparser.moduleJNISuffix", null);
        request.params.moduleWebSuffix = property("jparser.moduleWebSuffix", null);
        request.params.moduleFFMSuffix = property("jparser.moduleFFMSuffix", null);
        request.params.moduleCSuffix = property("jparser.moduleCSuffix", null);
        request.params.teaVMCLinkage = teaVMCLinkageProperty(
                "jparser.teaVMCLinkage", request.params.teaVMCLinkage);

        request.keepGeneratedCommandComments = booleanProperty("jparser.keepGeneratedCommandComments", false);
        request.targetConfig.addRuntimeHelperIDL = booleanProperty("jparser.addRuntimeHelperIDL", true);
        request.targetConfig.runtimeHelperMode = booleanProperty("jparser.runtimeHelperMode", false);
        request.targetConfig.windowsDebugBuild = booleanProperty("jparser.windowsDebugBuild", false);
        request.jniSymbolNameMode = symbolNameModeProperty("jparser.jniSymbolNameMode");
        request.ffmSymbolNameMode = symbolNameModeProperty("jparser.ffmSymbolNameMode");
        request.teaVMCSymbolNameMode = symbolNameModeProperty("jparser.teaVMCSymbolNameMode");
        request.ffmLogMethod = booleanProperty("jparser.ffmLogMethod", false);
        request.ffmDefaultCritical = booleanProperty("jparser.ffmDefaultCritical", false);

        request.targetConfig.jniCppStandard = property("jparser.jniCppStandard", request.targetConfig.jniCppStandard);
        request.targetConfig.ffmCppStandard = property("jparser.ffmCppStandard", request.targetConfig.ffmCppStandard);
        request.targetConfig.teaVMCCppStandard = property("jparser.teaVMCCppStandard", request.targetConfig.teaVMCCppStandard);
        request.targetConfig.webCppStandard = property("jparser.webCppStandard", request.targetConfig.webCppStandard);
        request.targetConfig.sourceLanguage = SourceLanguage.valueOf(property("jparser.sourceLanguage", request.targetConfig.sourceLanguage.name()).toUpperCase(Locale.ROOT));
        request.targetConfig.cStandard = property("jparser.cStandard", request.targetConfig.cStandard);
        request.targetConfig.webMainModuleName = property("jparser.webMainModuleName", request.targetConfig.webMainModuleName);
        request.targetConfig.webSideModule = intProperty("jparser.webSideModule", request.targetConfig.webSideModule);
        request.targetConfig.webForcedInclude = property("jparser.webForcedInclude", null);
        request.targetConfig.webMainModule = booleanProperty("jparser.webMainModule", request.targetConfig.webMainModule);
        addLines(request.targetConfig.webExportedFunctions, property("jparser.webExportedFunctions", null));
        addLines(request.targetConfig.webExportedRuntimeMethods, property("jparser.webExportedRuntimeMethods", null));
        request.targetConfig.androidApiLevel = AndroidTarget.ApiLevel.valueOf(property("jparser.androidApiLevel", request.targetConfig.androidApiLevel.name()));
        String androidTargets = property("jparser.androidTargets", null);
        if(androidTargets != null) {
            request.targetConfig.androidTargets.clear();
            String[] values = androidTargets.split(",");
            for(int i = 0; i < values.length; i++) {
                String value = values[i].trim();
                if(!value.isEmpty()) {
                    request.targetConfig.androidTargets.add(AndroidTarget.Target.valueOf(value));
                }
            }
        }

        addLines(request.additionalIDLPaths, property("jparser.additionalIDLPaths", null));
        addLines(request.additionalIDLRefPaths, property("jparser.additionalIDLRefPaths", null));
        addLines(request.additionalSourceDirs, property("jparser.additionalSourceDirs", null));
        addLines(request.additionalJavaImportPackages, property("jparser.additionalJavaImportPackages", null));
        addLines(request.additionalJavaClassPaths, property("jparser.additionalJavaClassPaths", null));
        fillHooks(request.targetConfig.globalHooks, "jparser.native");

        String configuredTargets = property("jparser.native.targets", null);
        if(configuredTargets != null) {
            String[] targetNames = configuredTargets.split(",");
            for(int i = 0; i < targetNames.length; i++) {
                String targetName = targetNames[i].trim();
                if(!targetName.isEmpty()) {
                    fillHooks(request.targetConfig.target(targetName), "jparser.native." + targetName);
                }
            }
        }
        fillAndroidTargetHooks(request.targetConfig, "android_jni");
        fillAndroidTargetHooks(request.targetConfig, "android_teavm_c");
        fillTeaVMCConsumers(request);
        return request;
    }

    private static void fillTeaVMCConsumers(JParserBuildRequest request) {
        int consumerCount = intProperty("jparser.teaVMCConsumers.count", 0);
        for(int consumerIndex = 0; consumerIndex < consumerCount; consumerIndex++) {
            String prefix = "jparser.teaVMCConsumers." + consumerIndex;
            TeaVMCConsumerConfig consumer = new TeaVMCConsumerConfig();
            consumer.targetName = property(prefix + ".targetName", null);
            consumer.variantName = property(prefix + ".variantName", null);
            addLines(consumer.selectorResources, property(prefix + ".selectorResources", null));
            addLines(consumer.headerDirs, property(prefix + ".headerDirs", null));
            addLines(consumer.compileDefinitions, property(prefix + ".compileDefinitions", null));
            addLines(consumer.compileOptions, property(prefix + ".compileOptions", null));
            addLines(consumer.staticLinkLibraries, property(prefix + ".staticLinkLibraries", null));
            addLines(consumer.staticLinkOptions, property(prefix + ".staticLinkOptions", null));
            int libraryCount = intProperty(prefix + ".staticLibraries.count", 0);
            for(int libraryIndex = 0; libraryIndex < libraryCount; libraryIndex++) {
                String libraryPrefix = prefix + ".staticLibraries." + libraryIndex;
                consumer.staticLibraries.add(new TeaVMCConsumerConfig.StaticLibrary(
                        property(libraryPrefix + ".resourcePath", null),
                        propertyAllowBlank(libraryPrefix + ".overrideVariable", "")));
            }
            request.teaVMCConsumers.add(consumer);
        }
    }

    private static void fillAndroidTargetHooks(DefaultBuildTargetConfig config, String targetName) {
        for(AndroidTarget.Target target : AndroidTarget.Target.values()) {
            fillHooks(config.target(targetName + ":" + target.name()), "jparser.native." + targetName + "." + target.name());
        }
    }

    private static void fillHooks(DefaultBuildTargetConfig.TargetHooks hooks, String prefix) {
        addLines(hooks.headerDirs, property(prefix + ".headerDirs", null));
        addLines(hooks.cppIncludes, property(prefix + ".cppIncludes", null));
        addLines(hooks.cppExcludes, property(prefix + ".cppExcludes", null));
        addLines(hooks.compileFlags, property(prefix + ".compileFlags", null));
        addLines(hooks.linkerFlags, property(prefix + ".linkerFlags", null));
        addLines(hooks.staticLinkerInputs, property(prefix + ".staticLinkerInputs", null));
        addLines(hooks.sharedLinkerInputs, property(prefix + ".sharedLinkerInputs", null));
        addLines(hooks.forcedIncludes, property(prefix + ".forcedIncludes", null));
        addLines(hooks.webExportedFunctions, property(prefix + ".webExportedFunctions", null));
        addLines(hooks.webExportedRuntimeMethods, property(prefix + ".webExportedRuntimeMethods", null));
        hooks.includeDefaultSources = optionalBooleanProperty(prefix + ".includeDefaultSources");
        hooks.includeCustomSources = optionalBooleanProperty(prefix + ".includeCustomSources");
        String sideModule = property(prefix + ".webSideModule", null);
        if(sideModule != null && !sideModule.trim().isEmpty()) {
            hooks.webSideModule = Integer.valueOf(sideModule.trim());
        }
        hooks.webMainModuleName = property(prefix + ".webMainModuleName", null);
        hooks.outputDirectoryPrefix = property(prefix + ".outputDirectoryPrefix", null);
    }

    private static void validate(JParserBuildRequest request) {
        require("libName", request.params.libName);
        require("modulePrefix", request.params.modulePrefix, true);
        require("packageName", request.params.packageName);
        if(!request.targetConfig.runtimeHelperMode) {
            require("cppSourcePath", request.params.cppSourcePath);
        }
    }

    private static void require(String name, String value) {
        require(name, value, false);
    }

    private static void require(String name, String value, boolean allowEmpty) {
        if(value == null || value.trim().isEmpty()) {
            if(allowEmpty && value != null && value.isEmpty()) {
                return;
            }
            throw new IllegalArgumentException("jParser build request requires " + name);
        }
    }

    private static String property(String name, String fallback) {
        Properties properties = REQUEST_PROPERTIES.get();
        String value = properties == null ? System.getProperty(name) : properties.getProperty(name);
        if(value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private static String propertyAllowBlank(String name, String fallback) {
        Properties properties = REQUEST_PROPERTIES.get();
        String value = properties == null ? System.getProperty(name) : properties.getProperty(name);
        if(value == null) {
            return fallback;
        }
        return value.trim();
    }

    private static boolean booleanProperty(String name, boolean fallback) {
        String value = property(name, null);
        if(value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }

    private static Boolean optionalBooleanProperty(String name) {
        String value = property(name, null);
        if(value == null) {
            return null;
        }
        return Boolean.valueOf(Boolean.parseBoolean(value));
    }

    private static int intProperty(String name, int fallback) {
        String value = property(name, null);
        if(value == null) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    private static JParserSymbolNameMode symbolNameModeProperty(String name) {
        String value = property(name, null);
        if(value == null) {
            return null;
        }
        return JParserSymbolNameMode.valueOf(value);
    }

    private static TeaVMCLinkage teaVMCLinkageProperty(String name, TeaVMCLinkage fallback) {
        String value = property(name, null);
        if(value == null) {
            return fallback;
        }
        return TeaVMCLinkage.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private static JNIClassData.SymbolNameMode toJNISymbolNameMode(JParserSymbolNameMode mode) {
        return JNIClassData.SymbolNameMode.valueOf(mode.name());
    }

    private static FFMClassData.SymbolNameMode toFFMSymbolNameMode(JParserSymbolNameMode mode) {
        return FFMClassData.SymbolNameMode.valueOf(mode.name());
    }

    private static void addLines(ArrayList<String> out, String value) {
        if(value == null || value.trim().isEmpty()) {
            return;
        }
        String[] lines = value.split("\\r?\\n");
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if(!line.isEmpty()) {
                out.add(line);
            }
        }
    }
}
