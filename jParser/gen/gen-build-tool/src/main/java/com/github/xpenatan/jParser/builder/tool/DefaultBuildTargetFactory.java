package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import com.github.xpenatan.jParser.builder.targets.EmscriptenTarget;
import com.github.xpenatan.jParser.builder.targets.IOSTarget;
import com.github.xpenatan.jParser.builder.targets.LinuxTarget;
import com.github.xpenatan.jParser.builder.targets.MacTarget;
import com.github.xpenatan.jParser.builder.targets.SourceLanguage;
import com.github.xpenatan.jParser.builder.targets.WindowsMSVCTarget;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.io.File;
import java.util.ArrayList;

/**
 * Standard jParser native build target matrix used by Gradle plugin tasks and
 * by projects that do not need a custom hand-written BuildLib entrypoint.
 */
public class DefaultBuildTargetFactory {

    public void addTargets(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets) {
        addTargets(op, idlReader, targets, DefaultBuildTargetConfig.fromBuildToolOptions(op));
    }

    public void addTargets(BuildToolOptions op, IDLReader idlReader, ArrayList<BuildMultiTarget> targets, DefaultBuildTargetConfig config) {
        if(op.containsArg("web_wasm")) {
            targets.add(getTeavmTarget(op, idlReader, config, "web_wasm"));
        }
        addJvmTargets(op, targets, config, "jni", false);
        addJvmTargets(op, targets, config, "ffm", true);
        addJvmTargets(op, targets, config, "teavm_c", false);
        if(op.containsArg("android_jni")) {
            targets.add(getAndroidTarget(op, config, "jni", "android_jni"));
        }
        if(op.containsArg("android_teavm_c")) {
            targets.add(getAndroidTarget(op, config, "teavm_c", "android_teavm_c"));
        }
        if(op.containsArg("ios_jni")) {
            targets.add(getIOSTarget(op, config, "jni", "ios_jni"));
        }
        if(op.containsArg("ios_teavm_c")) {
            targets.add(getIOSTarget(op, config, "teavm_c", "ios_teavm_c"));
        }
    }

    private void addJvmTargets(BuildToolOptions op, ArrayList<BuildMultiTarget> targets, DefaultBuildTargetConfig config, String api, boolean isFFM) {
        if(op.containsArg("windows64_" + api)) {
            targets.add(getWindowVCTarget(op, config, api, isFFM, "windows64_" + api));
        }
        if(op.containsArg("linux64_" + api)) {
            targets.add(getLinuxTarget(op, config, api, isFFM, "linux64_" + api));
        }
        if(op.containsArg("mac64_" + api)) {
            targets.add(getMacTarget(op, config, false, api, isFFM, "mac64_" + api));
        }
        if(op.containsArg("macArm_" + api)) {
            targets.add(getMacTarget(op, config, true, api, isFFM, "macArm_" + api));
        }
    }

    private BuildMultiTarget getWindowVCTarget(BuildToolOptions op, DefaultBuildTargetConfig config, String api, boolean isFFM, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        WindowsMSVCTarget compileStaticTarget = new WindowsMSVCTarget(config.sourceLanguage);
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        addSourceStandard(compileStaticTarget.cppFlags, api, true, config);
        applyFFMWindowsCompileFlags(compileStaticTarget, isFFM, config.ffmNative);
        addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        applyCompileHooks(compileStaticTarget, config, targetArg);
        multiTarget.add(compileStaticTarget);

        WindowsMSVCTarget linkTarget = new WindowsMSVCTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, true, config);
        applyFFMWindowsCompileFlags(linkTarget, isFFM, config.ffmNative);
        addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        linkTarget.linkerFlags.add("/WHOLEARCHIVE:" + ownStaticLibPath(op, "windows", api));
        linkTarget.linkerFlags.add("-DLL");
        applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.WINDOWS_WHOLE_ARCHIVE);
        applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg);
        applyFFMWindowsLinkFlags(linkTarget, isFFM, config.ffmNative);
        applyLinkHooks(linkTarget, config, targetArg);
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private BuildMultiTarget getLinuxTarget(BuildToolOptions op, DefaultBuildTargetConfig config, String api, boolean isFFM, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        LinuxTarget compileStaticTarget = new LinuxTarget(config.sourceLanguage);
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        addSourceStandard(compileStaticTarget.cppFlags, api, false, config);
        addFlagIfMissing(compileStaticTarget.cppFlags, "-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, config.ffmNative);
        addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        applyCompileHooks(compileStaticTarget, config, targetArg);
        multiTarget.add(compileStaticTarget);

        LinuxTarget linkTarget = new LinuxTarget();
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false, config);
        addFlagIfMissing(linkTarget.cppFlags, "-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, config.ffmNative);
        addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(ownStaticLibPath(op, "linux", api));
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.UNIX_WHOLE_ARCHIVE);
        applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg);
        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM, config.ffmNative);
        applyLinkHooks(linkTarget, config, targetArg);
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private BuildMultiTarget getMacTarget(BuildToolOptions op, DefaultBuildTargetConfig config, boolean isArm, String api, boolean isFFM, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        MacTarget compileStaticTarget = new MacTarget(config.sourceLanguage, isArm);
        compileStaticTarget.libDirSuffix += api;
        compileStaticTarget.isStatic = true;
        addSourceStandard(compileStaticTarget.cppFlags, api, false, config);
        addFlagIfMissing(compileStaticTarget.cppFlags, "-fPIC");
        applyFFMUnixCompileFlags(compileStaticTarget.cppFlags, isFFM, config.ffmNative);
        addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        applyCompileHooks(compileStaticTarget, config, targetArg);
        multiTarget.add(compileStaticTarget);

        MacTarget linkTarget = new MacTarget(isArm);
        linkTarget.libDirSuffix += api;
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false, config);
        addFlagIfMissing(linkTarget.cppFlags, "-fPIC");
        applyFFMUnixCompileFlags(linkTarget.cppFlags, isFFM, config.ffmNative);
        addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        linkTarget.linkerFlags.add("-Wl,-force_load");
        linkTarget.linkerFlags.add(ownStaticLibPath(op, isArm ? "macArm" : "mac", api));
        applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.MAC_FORCE_LOAD);
        applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg);
        applyFFMUnixLinkFlags(linkTarget.linkerFlags, isFFM, config.ffmNative);
        applyLinkHooks(linkTarget, config, targetArg);
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private BuildMultiTarget getTeavmTarget(BuildToolOptions op, IDLReader idlReader, DefaultBuildTargetConfig config, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        EmscriptenTarget compileStaticTarget = new EmscriptenTarget(config.sourceLanguage);
        compileStaticTarget.isStatic = true;
        compileStaticTarget.compileGlueCode = false;
        addSourceStandard(compileStaticTarget.cppFlags, "web", false, config);
        addFlagIfMissing(compileStaticTarget.cppFlags, "-fPIC");
        addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        applyCompileHooks(compileStaticTarget, config, targetArg);
        multiTarget.add(compileStaticTarget);

        EmscriptenTarget linkTarget = new EmscriptenTarget();
        linkTarget.idlReader = idlReader;
        addCppStandard(linkTarget.cppFlags, "web", false, config);
        addFlagIfMissing(linkTarget.cppFlags, "-fPIC");
        addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        addForcedInclude(linkTarget, config.webForcedInclude != null ? config.webForcedInclude : findCustomInclude(op, "CustomCode.h", "IDLCustomCode.h"));
        linkTarget.linkerFlags.add("-Wl,--whole-archive");
        linkTarget.linkerFlags.add(op.getModuleBuildCPPPath() + "/libs/emscripten/" + op.libName + "_.a");
        linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
        applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.UNIX_WHOLE_ARCHIVE);
        applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg);
        if(config.runtimeHelperMode) {
            addRuntimeWebDefaults(linkTarget);
        }
        else if(config.webMainModule) {
            addFlagIfMissing(linkTarget.linkerFlags, "-sMAIN_MODULE=1");
        }
        else {
            String mainModuleName = hook(config, targetArg).webMainModuleName != null ? hook(config, targetArg).webMainModuleName : config.webMainModuleName;
            Integer sideModule = hook(config, targetArg).webSideModule != null ? hook(config, targetArg).webSideModule : config.webSideModule;
            if(mainModuleName != null && !mainModuleName.trim().isEmpty()) {
                linkTarget.mainModuleName = mainModuleName;
            }
            if(sideModule != null && sideModule.intValue() > 0) {
                linkTarget.linkerFlags.add("-sSIDE_MODULE=" + sideModule);
            }
        }
        applyWebExports(linkTarget, config, targetArg);
        applyLinkHooks(linkTarget, config, targetArg);
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private BuildMultiTarget getAndroidTarget(BuildToolOptions op, DefaultBuildTargetConfig config, String api, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        for(int i = 0; i < config.androidTargets.size(); i++) {
            AndroidTarget.Target target = config.androidTargets.get(i);

            AndroidTarget compileStaticTarget = new AndroidTarget(config.sourceLanguage, target, config.androidApiLevel);
            if(api.equals("teavm_c")) {
                compileStaticTarget.libDirSuffix += api;
            }
            compileStaticTarget.isStatic = true;
            addSourceStandard(compileStaticTarget.cppFlags, api, false, config);
            addFlagIfMissing(compileStaticTarget.cppFlags, "-fPIC");
            addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
            applyCompileHooks(compileStaticTarget, config, targetArg);
            multiTarget.add(compileStaticTarget);

            AndroidTarget linkTarget = new AndroidTarget(target, config.androidApiLevel);
            if(api.equals("teavm_c")) {
                linkTarget.libDirSuffix += api;
            }
            setupGlueCode(linkTarget, api, libBuildCPPPath);
            addCppStandard(linkTarget.cppFlags, api, false, config);
            addFlagIfMissing(linkTarget.cppFlags, "-fPIC");
            addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
            linkTarget.linkerFlags.add("-Wl,--whole-archive");
            linkTarget.linkerFlags.add(ownStaticLibPath(op, target, api));
            linkTarget.linkerFlags.add("-Wl,--no-whole-archive");
            linkTarget.linkerFlags.add("-Wl,-z,max-page-size=16384");
            applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.UNIX_WHOLE_ARCHIVE, target.getFolder());
            applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg, target.getFolder());
            applyLinkHooks(linkTarget, config, targetArg);
            multiTarget.add(linkTarget);
        }
        return multiTarget;
    }

    private BuildMultiTarget getIOSTarget(BuildToolOptions op, DefaultBuildTargetConfig config, String api, String targetArg) {
        BuildMultiTarget multiTarget = new BuildMultiTarget();
        String sourceDir = op.getSourceDir();
        String libBuildCPPPath = op.getModuleBuildCPPPath();

        IOSTarget compileStaticTarget = new IOSTarget(config.sourceLanguage);
        if(api.equals("teavm_c")) {
            compileStaticTarget.libDirSuffix += api;
        }
        compileStaticTarget.isStatic = true;
        addSourceStandard(compileStaticTarget.cppFlags, api, false, config);
        addDefaultSources(compileStaticTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        applyCompileHooks(compileStaticTarget, config, targetArg);
        multiTarget.add(compileStaticTarget);

        IOSTarget linkTarget = new IOSTarget();
        if(api.equals("teavm_c")) {
            linkTarget.libDirSuffix += api;
        }
        setupGlueCode(linkTarget, api, libBuildCPPPath);
        addCppStandard(linkTarget.cppFlags, api, false, config);
        addDefaultHeaders(linkTarget, sourceDir, op.getCustomSourceDir(), libBuildCPPPath, config, targetArg);
        linkTarget.linkerFlags.add("-Wl,-force_load");
        linkTarget.linkerFlags.add(ownStaticLibPath(op, "ios", api));
        applyStaticLinkerInputs(linkTarget.linkerFlags, config, targetArg, LinkStyle.MAC_FORCE_LOAD);
        applySharedLinkerInputs(linkTarget.linkerFlags, config, targetArg);
        applyLinkHooks(linkTarget, config, targetArg);
        multiTarget.add(linkTarget);
        return multiTarget;
    }

    private void addDefaultSources(DefaultBuildTarget target, String sourceDir, String customSourceDir, String libBuildCPPPath, DefaultBuildTargetConfig config, String targetArg) {
        addDefaultHeaders(target, sourceDir, customSourceDir, libBuildCPPPath, config, targetArg);
        DefaultBuildTargetConfig.TargetHooks hooks = hook(config, targetArg);
        boolean includeDefaultSources = hooks.includeDefaultSources != null ? hooks.includeDefaultSources.booleanValue() : true;
        boolean includeCustomSources = hooks.includeCustomSources != null ? hooks.includeCustomSources.booleanValue() : true;
        if(config.runtimeHelperMode && libBuildCPPPath != null && !libBuildCPPPath.trim().isEmpty()) {
            target.cppInclude.add(libBuildCPPPath + "/src/runtime/RuntimeHelper.cpp");
        }
        if(includeDefaultSources && sourceDir != null && !sourceDir.trim().isEmpty()) {
            target.cppInclude.add(sourceDir + "**.cpp");
        }
        if(includeCustomSources && customSourceDir != null && !customSourceDir.trim().isEmpty()) {
            target.cppInclude.add(customSourceDir + "*.cpp");
        }
    }

    private void addDefaultHeaders(DefaultBuildTarget target, String sourceDir, String customSourceDir, String libBuildCPPPath, DefaultBuildTargetConfig config, String targetArg) {
        if(sourceDir != null && !sourceDir.trim().isEmpty()) {
            addHeaderDir(target, sourceDir);
        }
        if(customSourceDir != null && !customSourceDir.trim().isEmpty()) {
            addHeaderDir(target, customSourceDir);
        }
        if(config.runtimeHelperMode && libBuildCPPPath != null && !libBuildCPPPath.trim().isEmpty()) {
            addHeaderDir(target, libBuildCPPPath + "/src/runtime/");
            addHeaderDir(target, libBuildCPPPath + "/src/idl/");
        }
        applyHeaderHooks(target, config, targetArg);
    }

    private void applyCompileHooks(DefaultBuildTarget target, DefaultBuildTargetConfig config, String targetArg) {
        addAll(target.cppInclude, config.globalHooks.cppIncludes);
        addAll(target.cppExclude, config.globalHooks.cppExcludes);
        addAll(target.cppFlags, config.globalHooks.compileFlags);
        applyForcedIncludes(target, config.globalHooks.forcedIncludes);
        DefaultBuildTargetConfig.TargetHooks hooks = hook(config, targetArg);
        addAll(target.cppInclude, hooks.cppIncludes);
        addAll(target.cppExclude, hooks.cppExcludes);
        addAll(target.cppFlags, hooks.compileFlags);
        applyForcedIncludes(target, hooks.forcedIncludes);
    }

    private void applyLinkHooks(DefaultBuildTarget target, DefaultBuildTargetConfig config, String targetArg) {
        addAll(target.cppFlags, config.globalHooks.compileFlags);
        addAll(target.linkerFlags, config.globalHooks.linkerFlags);
        applyForcedIncludes(target, config.globalHooks.forcedIncludes);
        DefaultBuildTargetConfig.TargetHooks hooks = hook(config, targetArg);
        addAll(target.cppFlags, hooks.compileFlags);
        addAll(target.linkerFlags, hooks.linkerFlags);
        applyForcedIncludes(target, hooks.forcedIncludes);
    }

    private void applyHeaderHooks(DefaultBuildTarget target, DefaultBuildTargetConfig config, String targetArg) {
        for(int i = 0; i < config.globalHooks.headerDirs.size(); i++) {
            addHeaderDir(target, config.globalHooks.headerDirs.get(i));
        }
        DefaultBuildTargetConfig.TargetHooks hooks = hook(config, targetArg);
        for(int i = 0; i < hooks.headerDirs.size(); i++) {
            addHeaderDir(target, hooks.headerDirs.get(i));
        }
    }

    private DefaultBuildTargetConfig.TargetHooks hook(DefaultBuildTargetConfig config, String targetArg) {
        DefaultBuildTargetConfig.TargetHooks hooks = config.findTarget(targetArg);
        if(hooks == null) {
            hooks = new DefaultBuildTargetConfig.TargetHooks();
        }
        return hooks;
    }

    private void applyStaticLinkerInputs(ArrayList<String> flags, DefaultBuildTargetConfig config, String targetArg, LinkStyle style) {
        applyStaticLinkerInputs(flags, config, targetArg, style, null);
    }

    private void applySharedLinkerInputs(ArrayList<String> flags, DefaultBuildTargetConfig config, String targetArg) {
        applySharedLinkerInputs(flags, config, targetArg, null);
    }

    private void applyStaticLinkerInputs(ArrayList<String> flags, DefaultBuildTargetConfig config, String targetArg, LinkStyle style, String androidAbi) {
        addLinkerInputs(flags, config.globalHooks.staticLinkerInputs, style, androidAbi);
        addLinkerInputs(flags, hook(config, targetArg).staticLinkerInputs, style, androidAbi);
    }

    private void applySharedLinkerInputs(ArrayList<String> flags, DefaultBuildTargetConfig config, String targetArg, String androidAbi) {
        addSharedLinkerInputs(flags, config.globalHooks.sharedLinkerInputs, androidAbi);
        addSharedLinkerInputs(flags, hook(config, targetArg).sharedLinkerInputs, androidAbi);
    }

    private void applyWebExports(EmscriptenTarget target, DefaultBuildTargetConfig config, String targetArg) {
        addAllIfMissing(target.exportedFunctions, config.webExportedFunctions);
        addAllIfMissing(target.exportedRuntimeMethods, config.webExportedRuntimeMethods);
        DefaultBuildTargetConfig.TargetHooks hooks = hook(config, targetArg);
        addAllIfMissing(target.exportedFunctions, hooks.webExportedFunctions);
        addAllIfMissing(target.exportedRuntimeMethods, hooks.webExportedRuntimeMethods);
    }

    private void addRuntimeWebDefaults(EmscriptenTarget target) {
        addFlagIfMissing(target.linkerFlags, "--use-port=emdawnwebgpu");
        addFlagIfMissing(target.linkerFlags, "-sMAIN_MODULE=1");
        addIfMissing(target.exportedRuntimeMethods, "WebGPU");
        addIfMissing(target.exportedFunctions, "_free");
        addIfMissing(target.exportedFunctions, "_malloc");
        addIfMissing(target.exportedFunctions, "__ZNSt3__24coutE");
        addIfMissing(target.exportedFunctions, "___stack_low");
        addIfMissing(target.exportedFunctions, "___stack_high");
    }

    private void addLinkerInputs(ArrayList<String> flags, ArrayList<String> inputs, LinkStyle style) {
        addLinkerInputs(flags, inputs, style, null);
    }

    private void addLinkerInputs(ArrayList<String> flags, ArrayList<String> inputs, LinkStyle style, String androidAbi) {
        for(int i = 0; i < inputs.size(); i++) {
            String input = resolvePlaceholders(inputs.get(i), androidAbi);
            if(style == LinkStyle.WINDOWS_WHOLE_ARCHIVE) {
                flags.add("/WHOLEARCHIVE:" + input);
            }
            else if(style == LinkStyle.MAC_FORCE_LOAD) {
                flags.add("-Wl,-force_load");
                flags.add(input);
            }
            else {
                flags.add("-Wl,--whole-archive");
                flags.add(input);
                flags.add("-Wl,--no-whole-archive");
            }
        }
    }

    private void addSharedLinkerInputs(ArrayList<String> flags, ArrayList<String> inputs, String androidAbi) {
        for(int i = 0; i < inputs.size(); i++) {
            String input = resolvePlaceholders(inputs.get(i), androidAbi);
            if(androidAbi != null && isSharedObjectPath(input)) {
                addAndroidSharedObjectInput(flags, input);
            }
            else {
                flags.add(input);
            }
        }
    }

    private boolean isSharedObjectPath(String input) {
        return !input.startsWith("-") && input.endsWith(".so");
    }

    private void addAndroidSharedObjectInput(ArrayList<String> flags, String input) {
        String normalized = input.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        String directory = separator >= 0 ? normalized.substring(0, separator) : "";
        String fileName = separator >= 0 ? normalized.substring(separator + 1) : normalized;
        if(!directory.isEmpty()) {
            flags.add("-L" + directory);
        }
        flags.add("-l:" + fileName);
    }

    private void setupGlueCode(DefaultBuildTarget target, String api, String libBuildCPPPath) {
        if(api.equals("ffm")) {
            target.setupFFMGlueCode(libBuildCPPPath);
        }
        else if(api.equals("teavm_c")) {
            target.setupTeaVMCGlueCode(libBuildCPPPath);
        }
        else {
            target.setupJNIGlueCode(libBuildCPPPath);
        }
    }

    private void addCppStandard(ArrayList<String> flags, String api, boolean windows, DefaultBuildTargetConfig config) {
        String standard;
        if(api.equals("teavm_c")) {
            standard = config.teaVMCCppStandard;
        }
        else if(api.equals("ffm")) {
            standard = config.ffmCppStandard;
        }
        else if(api.equals("web")) {
            standard = config.webCppStandard;
        }
        else {
            standard = config.jniCppStandard;
        }
        flags.add((windows ? "/std:" : "-std=") + standard);
    }

    private void addSourceStandard(ArrayList<String> flags, String api, boolean windows, DefaultBuildTargetConfig config) {
        if(config.sourceLanguage == SourceLanguage.C) {
            flags.add((windows ? "/std:" : "-std=") + config.cStandard);
        }
        else {
            addCppStandard(flags, api, windows, config);
        }
    }

    private String ownStaticLibPath(BuildToolOptions op, String platform, String api) {
        String libBuildCPPPath = op.getModuleBuildCPPPath();
        if(platform.equals("windows")) {
            return libBuildCPPPath + "/libs/windows/vc/" + api + "/" + op.libName + "64_.lib";
        }
        if(platform.equals("linux")) {
            return libBuildCPPPath + "/libs/linux/" + api + "/lib" + op.libName + "64_.a";
        }
        if(platform.equals("macArm")) {
            return libBuildCPPPath + "/libs/mac/arm/" + api + "/lib" + op.libName + "64_.a";
        }
        if(platform.equals("mac")) {
            return libBuildCPPPath + "/libs/mac/" + api + "/lib" + op.libName + "64_.a";
        }
        if(platform.equals("ios")) {
            return libBuildCPPPath + "/libs/ios/" + (api.equals("teavm_c") ? api + "/" : "") + "lib" + op.libName + "_.a";
        }
        return libBuildCPPPath + "/libs/" + platform + "/" + api + "/lib" + op.libName + "64_.a";
    }

    private String ownStaticLibPath(BuildToolOptions op, AndroidTarget.Target target, String api) {
        String staticLibPath = op.getModuleBuildCPPPath() + "/libs/android/" + target.getFolder() + "/";
        if(api.equals("teavm_c")) {
            staticLibPath += api + "/";
        }
        return staticLibPath + "lib" + op.libName + ".a";
    }

    private void applyFFMWindowsCompileFlags(WindowsMSVCTarget target, boolean isFFM, DefaultBuildTargetConfig.FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.optimize) {
            addFlagIfMissing(target.cppFlags, "/O2");
        }
        if(ffmNativeBuildConfig.lto || ffmNativeBuildConfig.pgoGenerate || ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(target.cppFlags, "/GL");
        }
    }

    private void applyFFMWindowsLinkFlags(WindowsMSVCTarget target, boolean isFFM, DefaultBuildTargetConfig.FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(target.linkerFlags, "/LTCG");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGINSTRUMENT");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(target.linkerFlags, "/LTCG:PGOPTIMIZE");
        }
    }

    private void applyFFMUnixCompileFlags(ArrayList<String> flags, boolean isFFM, DefaultBuildTargetConfig.FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.optimize) {
            addFlagIfMissing(flags, "-O3");
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(flags, "-flto");
        }
        if(ffmNativeBuildConfig.hiddenVisibility) {
            addFlagIfMissing(flags, "-fvisibility=hidden");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private void applyFFMUnixLinkFlags(ArrayList<String> flags, boolean isFFM, DefaultBuildTargetConfig.FFMNativeBuildConfig ffmNativeBuildConfig) {
        if(!isFFM) {
            return;
        }
        if(ffmNativeBuildConfig.lto) {
            addFlagIfMissing(flags, "-flto");
        }
        if(ffmNativeBuildConfig.pgoGenerate) {
            addFlagIfMissing(flags, "-fprofile-generate");
        }
        else if(ffmNativeBuildConfig.pgoUse) {
            addFlagIfMissing(flags, "-fprofile-use");
        }
    }

    private String findCustomInclude(BuildToolOptions op, String first, String second) {
        String firstPath = op.getCustomSourceDir() + first;
        if(new File(firstPath).isFile()) {
            return firstPath;
        }
        String secondPath = op.getCustomSourceDir() + second;
        if(new File(secondPath).isFile()) {
            return secondPath;
        }
        return null;
    }

    private void addForcedInclude(DefaultBuildTarget target, String includePath) {
        if(includePath != null && !includePath.trim().isEmpty()) {
            target.headerDirs.add("-include" + includePath);
        }
    }

    private void applyForcedIncludes(DefaultBuildTarget target, ArrayList<String> includes) {
        for(int i = 0; i < includes.size(); i++) {
            addForcedInclude(target, includes.get(i));
        }
    }

    private void addHeaderDir(DefaultBuildTarget target, String headerDir) {
        if(headerDir == null || headerDir.trim().isEmpty()) {
            return;
        }
        if(headerDir.startsWith("-I")) {
            target.headerDirs.add(headerDir);
        }
        else {
            target.headerDirs.add("-I" + headerDir);
        }
    }

    private void addAll(ArrayList<String> out, ArrayList<String> in) {
        for(int i = 0; i < in.size(); i++) {
            out.add(in.get(i));
        }
    }

    private String resolvePlaceholders(String value, String androidAbi) {
        if(androidAbi != null) {
            value = value.replace("{androidAbi}", androidAbi);
            value = value.replace("{abi}", androidAbi);
        }
        return value;
    }

    private void addAllIfMissing(ArrayList<String> out, ArrayList<String> in) {
        for(int i = 0; i < in.size(); i++) {
            addIfMissing(out, in.get(i));
        }
    }

    private void addIfMissing(ArrayList<String> values, String value) {
        if(!values.contains(value)) {
            values.add(value);
        }
    }

    private void addFlagIfMissing(ArrayList<String> flags, String flag) {
        if(!flags.contains(flag)) {
            flags.add(flag);
        }
    }

    private enum LinkStyle {
        WINDOWS_WHOLE_ARCHIVE,
        UNIX_WHOLE_ARCHIVE,
        MAC_FORCE_LOAD
    }
}
