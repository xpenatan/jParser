package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.idl.IDLRenaming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JParserBuildRequest {
    public final BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
    public final DefaultBuildTargetConfig targetConfig = new DefaultBuildTargetConfig();
    public final ArrayList<String> additionalIDLPaths = new ArrayList<>();
    public final ArrayList<String> additionalIDLRefPaths = new ArrayList<>();
    public final ArrayList<String> additionalSourceDirs = new ArrayList<>();
    public final ArrayList<String> additionalJavaImportPackages = new ArrayList<>();
    public final ArrayList<String> additionalJavaClassPaths = new ArrayList<>();
    public final ArrayList<TeaVMCConsumerConfig> teaVMCConsumers = new ArrayList<>();
    private final Map<String, Boolean> finalClassOverrides = new LinkedHashMap<>();

    public boolean keepGeneratedCommandComments;
    public boolean finalClass = true;
    public IDLRenaming idlRenaming;
    public JParserSymbolNameMode jniSymbolNameMode;
    public JParserSymbolNameMode ffmSymbolNameMode;
    public JParserSymbolNameMode teaVMCSymbolNameMode;
    public boolean ffmLogMethod;
    public boolean ffmDefaultCritical;
    public boolean generateCore = true;

    /**
     * Overrides the global {@link #finalClass} setting for one binding class.
     *
     * <p>An enabled override still cannot make a callback or a class with a known
     * child final.</p>
     */
    public void setFinalClass(String className, boolean enabled) {
        if(className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Final-class override name must not be blank");
        }
        finalClassOverrides.put(className.trim(), enabled);
    }

    public Map<String, Boolean> getFinalClassOverrides() {
        return Collections.unmodifiableMap(finalClassOverrides);
    }
}
