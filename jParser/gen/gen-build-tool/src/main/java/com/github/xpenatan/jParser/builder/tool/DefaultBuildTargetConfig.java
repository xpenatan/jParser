package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.targets.AndroidTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Declarative defaults and hooks used by {@link DefaultBuildTargetFactory}.
 */
public class DefaultBuildTargetConfig {

    public boolean addRuntimeHelperIDL = true;
    public boolean windowsDebugBuild = false;
    public boolean runtimeHelperMode = false;

    public String jniCppStandard = "c++11";
    public String ffmCppStandard = "c++11";
    public String teaVMCCppStandard = "c++17";
    public String webCppStandard = "c++11";

    public String webMainModuleName = "runtime";
    public int webSideModule = 2;
    public String webForcedInclude = null;
    public boolean webMainModule = false;
    public final ArrayList<String> webExportedFunctions = new ArrayList<>();
    public final ArrayList<String> webExportedRuntimeMethods = new ArrayList<>();

    public AndroidTarget.ApiLevel androidApiLevel = AndroidTarget.ApiLevel.Android_10_29;
    public final ArrayList<AndroidTarget.Target> androidTargets = new ArrayList<>();

    public final FFMNativeBuildConfig ffmNative = new FFMNativeBuildConfig();
    public final TargetHooks globalHooks = new TargetHooks();
    private final Map<String, TargetHooks> targetHooks = new HashMap<>();

    public DefaultBuildTargetConfig() {
        androidTargets.add(AndroidTarget.Target.x86);
        androidTargets.add(AndroidTarget.Target.x86_64);
        androidTargets.add(AndroidTarget.Target.armeabi_v7a);
        androidTargets.add(AndroidTarget.Target.arm64_v8a);
    }

    public TargetHooks target(String targetArg) {
        TargetHooks hooks = targetHooks.get(targetArg);
        if(hooks == null) {
            hooks = new TargetHooks();
            targetHooks.put(targetArg, hooks);
        }
        return hooks;
    }

    public TargetHooks findTarget(String targetArg) {
        return targetHooks.get(targetArg);
    }

    public static class FFMNativeBuildConfig {
        public boolean optimize;
        public boolean lto;
        public boolean hiddenVisibility;
        public boolean pgoGenerate;
        public boolean pgoUse;
    }

    public static class TargetHooks {
        public final ArrayList<String> headerDirs = new ArrayList<>();
        public final ArrayList<String> cppIncludes = new ArrayList<>();
        public final ArrayList<String> cppExcludes = new ArrayList<>();
        public final ArrayList<String> compileFlags = new ArrayList<>();
        public final ArrayList<String> linkerFlags = new ArrayList<>();
        public final ArrayList<String> staticLinkerInputs = new ArrayList<>();
        public final ArrayList<String> sharedLinkerInputs = new ArrayList<>();
        public final ArrayList<String> forcedIncludes = new ArrayList<>();
        public final ArrayList<String> webExportedFunctions = new ArrayList<>();
        public final ArrayList<String> webExportedRuntimeMethods = new ArrayList<>();
        public Boolean includeDefaultSources;
        public Boolean includeCustomSources;
        public Integer webSideModule;
        public String webMainModuleName;
    }
}
