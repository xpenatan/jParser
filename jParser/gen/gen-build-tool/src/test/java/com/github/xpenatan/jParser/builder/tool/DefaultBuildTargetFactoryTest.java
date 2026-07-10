package com.github.xpenatan.jParser.builder.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.DefaultBuildTarget;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultBuildTargetFactoryTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void windowsTeaVMCUsesTheCMakeCompatibleDynamicRuntime() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_teavm_c", "windows64_teavm_c");

        assertTrue(targets.size() == 1);
        assertTrue(targets.get(0).multiTarget.size() == 2);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertTrue(target.cppFlags.contains("/MD"));
        }
    }

    @Test
    public void windowsJniRuntimeChoiceRemainsUnchanged() throws Exception {
        ArrayList<BuildMultiTarget> targets = targetsFor("gen_jni", "windows64_jni");

        assertTrue(targets.size() == 1);
        for(int i = 0; i < targets.get(0).multiTarget.size(); i++) {
            DefaultBuildTarget target = (DefaultBuildTarget)targets.get(0).multiTarget.get(i);
            assertFalse(target.cppFlags.contains("/MD"));
        }
    }

    private ArrayList<BuildMultiTarget> targetsFor(String... args) throws Exception {
        BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
        params.libName = "PortableLib";
        params.packageName = "com.example.portable";
        params.webModuleName = "PortableLib";
        params.modulePath = temporaryFolder.newFolder().getAbsolutePath();

        BuildToolOptions options = new BuildToolOptions(params, args);
        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        new DefaultBuildTargetFactory().addTargets(options, new IDLReader(), targets);
        return targets;
    }
}
