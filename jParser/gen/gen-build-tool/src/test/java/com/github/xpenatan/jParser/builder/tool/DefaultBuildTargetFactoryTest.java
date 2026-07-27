package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.builder.targets.IOSTarget;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultBuildTargetFactoryTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void windowsTeaVMCDoesNotForceARuntimeFlag() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_teavm_c", "windows64_teavm_c");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 3);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertFalse(target.cppFlags.contains("/MD"));
            assertFalse(target.cppFlags.contains("/MT"));
        }
    }

    @Test
    public void windowsTeaVMCPassesThroughRawCompilerFlags() throws Exception {
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.target("windows64_teavm_c").compileFlags.add("/MD");

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_teavm_c", "windows64_teavm_c");

        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertTrue(target.cppFlags.contains("/MD"));
            assertFalse(target.cppFlags.contains("/MT"));
        }
    }

    @Test
    public void windowsJniRuntimeChoiceRemainsUnchanged() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_jni", "windows64_jni");

        assertTrue(targets.size() == 1);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertFalse(target.cppFlags.contains("/MD"));
            assertFalse(target.cppFlags.contains("/MT"));
        }
    }

    @Test
    public void desktopBuildCreatesPrecompiledBridgeArchiveBeforeStandaloneLibrary() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_ffm", "linux64_ffm");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 3);
        DefaultBuildTarget implementation = (DefaultBuildTarget)targets.get(0).multiTarget.get(0);
        DefaultBuildTarget bridge = (DefaultBuildTarget)targets.get(0).multiTarget.get(1);
        DefaultBuildTarget standalone = (DefaultBuildTarget)targets.get(0).multiTarget.get(2);

        assertTrue(implementation.isStatic);
        assertTrue(bridge.isStatic);
        assertTrue(bridge.libName.equals("PortableLib_bridge"));
        assertTrue(bridge.cppInclude.stream().anyMatch(path -> path.endsWith("/FFMGlue.cpp")));
        assertFalse(standalone.shouldCompile);
        assertTrue(standalone.linkerFlags.stream().anyMatch(path ->
                path.endsWith("/libPortableLib_bridge64_.a")));
    }

    @Test
    public void globalSourceSelectionDisablesAutomaticSources() throws Exception {
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.globalHooks.includeDefaultSources = false;
        config.globalHooks.includeCustomSources = false;
        config.globalHooks.cppIncludes.add("explicit.cpp");

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_jni", "windows64_jni");

        assertTrue(targets.get(0).multiTarget.stream()
                .map(target -> (DefaultBuildTarget)target)
                .anyMatch(target -> target.cppInclude.contains("explicit.cpp")));
        assertFalse(targets.get(0).multiTarget.stream()
                .map(target -> (DefaultBuildTarget)target)
                .anyMatch(target -> target.cppInclude.stream().anyMatch(path -> path.endsWith("**.cpp"))));
        assertFalse(targets.get(0).multiTarget.stream()
                .map(target -> (DefaultBuildTarget)target)
                .anyMatch(target -> target.cppInclude.stream().anyMatch(path ->
                        path.replace('\\', '/').contains("/src/main/cpp/custom/") && path.endsWith("*.cpp"))));
    }

    @Test
    public void targetSourceSelectionOverridesGlobalSourceSelection() throws Exception {
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.globalHooks.includeDefaultSources = false;
        config.globalHooks.includeCustomSources = false;
        config.target("windows64_jni").includeDefaultSources = true;
        config.target("windows64_jni").includeCustomSources = true;

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_jni", "windows64_jni");

        assertTrue(targets.get(0).multiTarget.stream()
                .map(target -> (DefaultBuildTarget)target)
                .anyMatch(target -> target.cppInclude.stream().anyMatch(path -> path.endsWith("**.cpp"))));
        assertTrue(targets.get(0).multiTarget.stream()
                .map(target -> (DefaultBuildTarget)target)
                .anyMatch(target -> target.cppInclude.stream().anyMatch(path ->
                        path.replace('\\', '/').contains("/src/main/cpp/custom/") && path.endsWith("*.cpp"))));
    }

    @Test
    public void windowsForcedIncludesUseMsvcSyntax() throws Exception {
        String includePath = temporaryFolder.newFile("force.h").getAbsolutePath();
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.target("windows64_teavm_c").forcedIncludes.add(includePath);

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_teavm_c", "windows64_teavm_c");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 3);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertTrue(target.headerDirs.contains("/FI" + includePath));
            assertFalse(target.headerDirs.contains("-include" + includePath));
        }
    }

    @Test
    public void nonMsvcForcedIncludesKeepGccClangSyntax() throws Exception {
        String includePath = temporaryFolder.newFile("force.h").getAbsolutePath();
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.target("linux64_teavm_c").forcedIncludes.add(includePath);

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_teavm_c", "linux64_teavm_c");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 3);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertTrue(target.headerDirs.contains("-include" + includePath));
            assertFalse(target.headerDirs.contains("/FI" + includePath));
        }
    }

    @Test
    public void iosTeaVMCBuildsOnlyTheThreeSupportedStaticLibrarySlices() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_teavm_c", "ios_teavm_c");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 9);
        assertIOSSlice((IOSTarget)targets.get(0).multiTarget.get(0),
                IOSTarget.SDK.DEVICE, IOSTarget.Architecture.ARM64);
        assertIOSSlice((IOSTarget)targets.get(0).multiTarget.get(3),
                IOSTarget.SDK.SIMULATOR, IOSTarget.Architecture.ARM64);
        assertIOSSlice((IOSTarget)targets.get(0).multiTarget.get(6),
                IOSTarget.SDK.SIMULATOR, IOSTarget.Architecture.X86_64);
        for(int i = 0; i < 9; i += 3) {
            DefaultBuildTarget implementation = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            DefaultBuildTarget bridge = (DefaultBuildTarget)targets.get(0).multiTarget.get(i + 1);
            IOSTarget combined = (IOSTarget)targets.get(0).multiTarget.get(i + 2);
            assertTrue(implementation.libName.equals("PortableLib_implementation"));
            assertTrue(bridge.libName.equals("PortableLib_bridge"));
            assertFalse(combined.shouldCompile);
            assertTrue(combined.staticArchiveInputs.size() == 2);
        }
    }

    @Test
    public void iosTeaVMCPassesTargetHooksToEverySlice() throws Exception {
        DefaultBuildTargetConfig config = new DefaultBuildTargetConfig();
        config.target("ios_teavm_c").compileFlags.add("-DPORTABLE_IOS=1");

        ArrayList<BuildMultiTarget> targets = targetsFor(config, "gen_teavm_c", "ios_teavm_c");

        for(int i = 0; i < targets.get(0).multiTarget.size(); i += 3) {
            DefaultBuildTarget implementation = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            DefaultBuildTarget bridge = (DefaultBuildTarget)targets.get(0).multiTarget.get(i + 1);
            assertTrue(implementation.cppFlags.contains("-DPORTABLE_IOS=1"));
            assertTrue(bridge.cppFlags.contains("-DPORTABLE_IOS=1"));
        }
    }

    private void assertIOSSlice(IOSTarget target, IOSTarget.SDK sdk, IOSTarget.Architecture architecture) {
        assertTrue(target.isStatic);
        assertTrue(target.getSdk() == sdk);
        assertTrue(target.getArchitecture() == architecture);
        assertTrue(target.libPrefix.equals("lib"));
        assertTrue(target.libDirSuffix.equals(target.getResourcePlatform() + "/teavm_c"));
        assertTrue(target.tempBuildDir.equals("target/" + target.getResourcePlatform() + "/"));
        assertTrue(target.cppCompiler.equals(java.util.Arrays.asList(
                "xcrun", "--sdk", sdk.getXcrunName(), "clang++")));
        assertTrue(target.cppFlags.contains("-arch"));
        assertTrue(target.cppFlags.contains(architecture.getClangName()));
        assertTrue(target.cppFlags.contains("-fPIC"));
        assertFalse(target.cppFlags.contains("-arch " + architecture.getClangName()));
    }

    private ArrayList<BuildMultiTarget> targetsFor(String... args) throws Exception {
        return targetsFor(new DefaultBuildTargetConfig(), args);
    }

    private ArrayList<BuildMultiTarget> targetsFor(DefaultBuildTargetConfig config, String... args) throws Exception {
        BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
        params.libName = "PortableLib";
        params.packageName = "com.example.portable";
        params.webModuleName = "PortableLib";
        params.modulePath = temporaryFolder.newFolder().getAbsolutePath();
        params.cppSourcePath = temporaryFolder.newFolder().getAbsolutePath();

        BuildToolOptions options = new BuildToolOptions(params, args);
        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        new DefaultBuildTargetFactory().addTargets(options, new IDLReader(), targets, config);
        return targets;
    }
}
