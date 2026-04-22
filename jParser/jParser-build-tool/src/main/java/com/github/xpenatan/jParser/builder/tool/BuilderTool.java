package com.github.xpenatan.jParser.builder.tool;

import com.github.xpenatan.jParser.builder.BuildConfig;
import com.github.xpenatan.jParser.builder.BuildMultiTarget;
import com.github.xpenatan.jParser.builder.JBuilder;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.cpp.CppCodeParser;
import com.github.xpenatan.jParser.cpp.CppGenerator;
import com.github.xpenatan.jParser.cpp.NativeCPPGenerator;
import com.github.xpenatan.jParser.ffm.FFMCodeParser;
import com.github.xpenatan.jParser.ffm.FFMCppGenerator;
import com.github.xpenatan.jParser.idl.IDLFile;
import com.github.xpenatan.jParser.idl.IDLRenaming;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.parser.IDLDefaultCodeParser;
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
        applyAutoGenerateFlags(op);

        IDLReader idlReader = IDLReader.readIDL(op.getIDL());

        IDLFile[] idlRefPath = op.getIDLRef();
        for(int i = 0; i < idlRefPath.length; i++) {
            IDLFile idlPath = idlRefPath[i];
            IDLReader.addIDLRef(idlReader, idlPath);
        }

        ArrayList<BuildMultiTarget> targets = new ArrayList<>();
        listener.onAddTarget(op, idlReader, targets);
        IDLReader.setupClasses(idlReader);

        if(op.generateCore) {
            IDLDefaultCodeParser coreParser = new IDLDefaultCodeParser(op.packageName, "CORE", idlReader, op.getSourceDir());
            coreParser.generateClass = true;
            coreParser.generateNativeBindings = false;
            coreParser.idlRenaming = packageRenaming;
            JParser.generate(coreParser, op.getModuleBaseJavaDir(), op.getModuleCorePath() + "/src/main/java");
        }

        if(op.generateDesktopJNI || op.generateAndroid) {
//            NativeCPPGenerator.SKIP_GLUE_CODE = true;
            CppGenerator cppGenerator = new NativeCPPGenerator(op.getCPPDestinationPath());
            String[] outputPaths = op.getJNIJavaOutputPaths();
            for(int i = 0; i < outputPaths.length; i++) {
                String outputPath = outputPaths[i];
                CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, op.packageName, op.getSourceDir());
                cppParser.generateClass = true;
                cppParser.idlRenaming = packageRenaming;
                JParser.generate(cppParser, op.getModuleBaseJavaDir(), outputPath);
            }
        }

        if(op.generateTeaVMWeb) {
//            EmscriptenTarget.SKIP_GLUE_CODE = true;
            TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, op.webModuleName, op.packageName, op.getSourceDir());
            teavmParser.idlRenaming = packageRenaming;
            JParser.generate(teavmParser, op.getModuleBaseJavaDir(), op.getModuleTeaVMPath() + "/src/main/java/");
        }

        if(op.generateDesktopFFM) {
            FFMCppGenerator ffmGenerator = new FFMCppGenerator(op.getCPPDestinationPath());
            FFMCodeParser ffmParser = new FFMCodeParser(ffmGenerator, idlReader, op.packageName, op.getSourceDir());
            ffmParser.generateClass = true;
            ffmParser.idlRenaming = packageRenaming;
            JParser.generate(ffmParser, op.getModuleBaseJavaDir(), op.getModuleFFMPath() + "/src/main/java");
        }

        BuildConfig buildConfig = new BuildConfig(op);
        JBuilder.build(buildConfig, targets);
    }

    private static void applyAutoGenerateFlags(BuildToolOptions op) {
        if(op.containsArg("gen_ffm_desktop") ||
                op.containsArg("ffm_windows64") ||
                op.containsArg("ffm_linux64") ||
                op.containsArg("ffm_mac64") ||
                op.containsArg("ffm_macArm")) {
            op.generateDesktopFFM = true;
        }

        if(op.containsArg("gen_jni_desktop") ||
                op.containsArg("jni_windows64") ||
                op.containsArg("jni_linux64") ||
                op.containsArg("jni_mac64") ||
                op.containsArg("jni_macArm") ||
                op.containsArg("jni_ios")) {
            op.generateDesktopJNI = true;
        }

        if(op.containsArg("gen_jni_android") || op.containsArg("jni_android")) {
            op.generateAndroid = true;
        }

        if(op.containsArg("gen_jni_ios") || op.containsArg("jni_ios")) {
            op.generateIOS = true;
        }

        if(op.containsArg("gen_teavm") || op.containsArg("teavm")) {
            op.generateTeaVMWeb = true;
        }
    }
}