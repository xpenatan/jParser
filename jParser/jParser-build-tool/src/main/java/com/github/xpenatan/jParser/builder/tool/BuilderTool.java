package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.JBuilder;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.cpp.CppCodeParser;
import com.github.xpenatan.jParser.cpp.CppGenerator;
import com.github.xpenatan.jParser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jParser.idl.IDLRenaming;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.teavm.TeaVMCodeParser;
import java.util.ArrayList;

public class BuilderTool {

    public static void build(BuildToolOptions op, BuildToolListener listener) {
        build(op, listener, null);
    }

    public static void build(BuildToolOptions op, BuildToolListener listener, IDLRenaming packageRenaming) {
        try {
            generateAndBuild(op, listener, packageRenaming);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateAndBuild(BuildToolOptions op, BuildToolListener listener, IDLRenaming packageRenaming) throws Exception {
        IDLReader idlReader = IDLReader.readIDL(op.getIDLPath());

        String[] idlRefPath = op.getIDLRefPath();
        for(int i = 0; i < idlRefPath.length; i++) {
            String idlPath = idlRefPath[i];
            IDLReader.addIDLRef(idlReader, idlPath);
        }

        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        listener.onAddTarget(op, idlReader, targets);
        IDLReader.setupClasses(idlReader);

        if(op.generateCPP) {
//            NativeCPPGenerator.SKIP_GLUE_CODE = true;
            CppGenerator cppGenerator = new NativeCPPGenerator(op.getCPPDestinationPath());
            CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, op.packageName, op.getSourceDir());
            cppParser.generateClass = true;
            cppParser.idlRenaming = packageRenaming;
            JParser.generate(cppParser, op.getModuleBaseJavaDir(), op.getModuleCorePath() + "/src/main/java");
        }

        if(op.generateTeaVM) {
//            EmscriptenTarget.SKIP_GLUE_CODE = true;
            TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, op.webModuleName, op.packageName, op.getSourceDir());
            teavmParser.idlRenaming = packageRenaming;
            JParser.generate(teavmParser, op.getModuleBaseJavaDir(), op.getModuleTeaVMPath() + "/src/main/java/");
        }

        BuildConfig buildConfig = new BuildConfig(op);
        JBuilder.build(buildConfig, targets);
    }
}