package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.cpp.JNIClassData;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;

public class JParserBuildRunner {

    public static void main(String[] args) {
        build(fromSystemProperties(), args);
    }

    public static void build(JParserBuildRequest request, String... args) {
        validate(request);
        boolean previousRuntimeHelperMode = JParser.CREATE_RUNTIME_HELPER;
        try {
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
            });
        }
        finally {
            JParser.CREATE_RUNTIME_HELPER = previousRuntimeHelperMode;
        }
    }

    private static JParserBuildRequest fromSystemProperties() {
        JParserBuildRequest request = new JParserBuildRequest();
        request.params.libName = property("jparser.libName", null);
        request.params.idlName = property("jparser.idlName", request.params.libName);
        if(request.params.idlName != null && request.params.idlName.trim().isEmpty()) {
            request.params.idlName = null;
        }
        request.params.webModuleName = property("jparser.webModuleName", request.params.libName);
        request.params.packageName = property("jparser.packageName", null);
        request.params.modulePrefix = property("jparser.modulePrefix", null);
        request.params.cppSourcePath = property("jparser.cppSourcePath", null);
        request.params.modulePath = property("jparser.modulePath", null);
        request.params.moduleBaseSuffix = property("jparser.moduleBaseSuffix", null);
        request.params.moduleBuildSuffix = property("jparser.moduleBuildSuffix", null);
        request.params.moduleCoreSuffix = property("jparser.moduleCoreSuffix", null);
        request.params.moduleJNISuffix = property("jparser.moduleJNISuffix", null);
        request.params.moduleWebSuffix = property("jparser.moduleWebSuffix", null);
        request.params.moduleFFMSuffix = property("jparser.moduleFFMSuffix", null);
        request.params.moduleCSuffix = property("jparser.moduleCSuffix", null);

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
        request.targetConfig.webMainModuleName = property("jparser.webMainModuleName", request.targetConfig.webMainModuleName);
        request.targetConfig.webSideModule = intProperty("jparser.webSideModule", request.targetConfig.webSideModule);
        request.targetConfig.webForcedInclude = property("jparser.webForcedInclude", null);
        request.targetConfig.webMainModule = booleanProperty("jparser.webMainModule", request.targetConfig.webMainModule);
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
        return request;
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
        hooks.includeDefaultSources = optionalBooleanProperty(prefix + ".includeDefaultSources");
        hooks.includeCustomSources = optionalBooleanProperty(prefix + ".includeCustomSources");
        String sideModule = property(prefix + ".webSideModule", null);
        if(sideModule != null && !sideModule.trim().isEmpty()) {
            hooks.webSideModule = Integer.valueOf(sideModule.trim());
        }
        hooks.webMainModuleName = property(prefix + ".webMainModuleName", null);
    }

    private static void validate(JParserBuildRequest request) {
        require("libName", request.params.libName);
        require("modulePrefix", request.params.modulePrefix);
        require("packageName", request.params.packageName);
        if(!request.targetConfig.runtimeHelperMode) {
            require("cppSourcePath", request.params.cppSourcePath);
        }
    }

    private static void require(String name, String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("jParser build request requires " + name);
        }
    }

    private static String property(String name, String fallback) {
        String value = System.getProperty(name);
        if(value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
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
