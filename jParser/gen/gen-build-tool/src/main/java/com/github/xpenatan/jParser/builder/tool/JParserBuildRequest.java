package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.idl.IDLRenaming;
import java.util.ArrayList;

public class JParserBuildRequest {
    public final BuildToolOptions.BuildToolParams params = new BuildToolOptions.BuildToolParams();
    public final DefaultBuildTargetConfig targetConfig = new DefaultBuildTargetConfig();
    public final ArrayList<String> additionalIDLPaths = new ArrayList<>();
    public final ArrayList<String> additionalIDLRefPaths = new ArrayList<>();
    public final ArrayList<String> additionalSourceDirs = new ArrayList<>();
    public final ArrayList<String> additionalJavaImportPackages = new ArrayList<>();
    public final ArrayList<TeaVMCConsumerConfig> teaVMCConsumers = new ArrayList<>();

    public boolean keepGeneratedCommandComments;
    public IDLRenaming idlRenaming;
    public JParserSymbolNameMode jniSymbolNameMode;
    public JParserSymbolNameMode ffmSymbolNameMode;
    public JParserSymbolNameMode teaVMCSymbolNameMode;
    public boolean ffmLogMethod;
    public boolean ffmDefaultCritical;
    public boolean generateCore = true;
}
