package com.github.xpenatan.jparser.builder.tool;

import com.github.xpenatan.jparser.builder.BuildConfig;
import com.github.xpenatan.jparser.builder.BuildMultiTarget;
import com.github.xpenatan.jparser.builder.JBuilder;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.cpp.CppCodeParser;
import com.github.xpenatan.jparser.cpp.CppGenerator;
import com.github.xpenatan.jparser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.teavm.TeaVMCodeParser;
import java.util.ArrayList;

public class BuilderTool {

    public static void build(BuildToolOptions op, BuildToolListener listener) {
        try {
            generateAndBuild(op, listener);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateAndBuild(BuildToolOptions op, BuildToolListener listener) throws Exception {
        op.setup();

        IDLReader idlReader = IDLReader.readIDL(op.getIDLPath());

        if(op.generateCPP) {
//            NativeCPPGenerator.SKIP_GLUE_CODE = true;
            CppGenerator cppGenerator = new NativeCPPGenerator(op.getCPPDestinationPath());
            CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, op.libBasePackage, op.getSourceDir());
            cppParser.generateClass = true;
            JParser.generate(cppParser, op.getModuleBaseJavaDir(), op.getModuleCorePath() + "/src/main/java");
        }

        if(op.generateTeaVM) {
//            EmscriptenTarget.SKIP_GLUE_CODE = true;
            TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, op.moduleName, op.libBasePackage, op.getSourceDir());
            JParser.generate(teavmParser, op.getModuleBaseJavaDir(), op.getModuleTeaVMPath() + "/src/main/java/");
        }

        ArrayList<BuildMultiTarget> targets = new ArrayList<>();

        listener.onAddTarget(op, idlReader, targets);

        BuildConfig buildConfig = new BuildConfig(op);
        JBuilder.build(buildConfig, targets);
    }
}