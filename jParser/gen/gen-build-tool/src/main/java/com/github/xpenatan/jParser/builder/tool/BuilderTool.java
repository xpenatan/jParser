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
import com.github.xpenatan.jParser.idl.parser.IDLClassGeneratorParser;
import com.github.xpenatan.jParser.c.TeaVMCCodeParser;
import com.github.xpenatan.jParser.c.TeaVMCGenerator;
import com.github.xpenatan.jParser.teavm.TeaVMCodeParser;
import java.io.File;
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
            applyAdditionalJavaImportPackages(coreParser, op);
            coreParser.setKeepGeneratedCommandComments(op.keepGeneratedCommandComments);
            coreParser.generateClass = true;
            coreParser.generateNativeBindings = false;
            coreParser.idlRenaming = packageRenaming;
            JParser.generate(coreParser, op.getModuleBaseJavaDir(), op.getModuleCorePath() + "/src/main/java");
        }

        if(op.generateJNI) {
//            NativeCPPGenerator.SKIP_GLUE_CODE = true;
            CppGenerator cppGenerator = new NativeCPPGenerator(op.getCPPDestinationPath());
            CppCodeParser cppParser = new CppCodeParser(cppGenerator, idlReader, op.packageName, op.getSourceDir());
            applyAdditionalJavaImportPackages(cppParser, op);
            cppParser.setJNIClassData(op.jniClassData);
            cppParser.setKeepGeneratedCommandComments(op.keepGeneratedCommandComments);
            cppParser.generateClass = true;
            cppParser.idlRenaming = packageRenaming;
            JParser.generate(cppParser, op.getModuleBaseJavaDir(), op.getJNIJavaOutputPath());
        }

        if(op.generateWeb) {
//            EmscriptenTarget.SKIP_GLUE_CODE = true;
            TeaVMCodeParser teavmParser = new TeaVMCodeParser(idlReader, op.webModuleName, op.packageName, op.getSourceDir());
            applyAdditionalJavaImportPackages(teavmParser, op);
            teavmParser.setKeepGeneratedCommandComments(op.keepGeneratedCommandComments);
            teavmParser.idlRenaming = packageRenaming;
            JParser.generate(teavmParser, op.getModuleBaseJavaDir(), op.getTeaVMJavaOutputPath());
        }

        if(op.generateTeaVMC) {
            TeaVMCGenerator teaVMCGenerator = new TeaVMCGenerator(op.getCPPDestinationPath());
            teaVMCGenerator.setFFMClassData(op.teaVMCClassData);
            addTeaVMCDefaultInclude(op, teaVMCGenerator);
            TeaVMCCodeParser teaVMCParser = new TeaVMCCodeParser(teaVMCGenerator, idlReader, op.packageName, op.getSourceDir());
            applyAdditionalJavaImportPackages(teaVMCParser, op);
            teaVMCParser.setKeepGeneratedCommandComments(op.keepGeneratedCommandComments);
            teaVMCParser.setSymbolData(op.teaVMCClassData);
            teaVMCParser.generateClass = true;
            teaVMCParser.idlRenaming = packageRenaming;
            JParser.generate(teaVMCParser, op.getModuleBaseJavaDir(), op.getCJavaOutputPath());
        }

        if(op.generateFFM) {
            FFMCppGenerator ffmGenerator = new FFMCppGenerator(op.getCPPDestinationPath());
            ffmGenerator.setFFMClassData(op.ffmClassData);
            FFMCodeParser ffmParser = new FFMCodeParser(ffmGenerator, idlReader, op.packageName, op.getSourceDir());
            applyAdditionalJavaImportPackages(ffmParser, op);
            ffmParser.setKeepGeneratedCommandComments(op.keepGeneratedCommandComments);
            ffmParser.setFFMClassData(op.ffmClassData);
            ffmParser.generateClass = true;
            ffmParser.idlRenaming = packageRenaming;
            JParser.generate(ffmParser, op.getModuleBaseJavaDir(), op.getFFMJavaOutputPath());
        }

        BuildConfig buildConfig = new BuildConfig(op);
        JBuilder.build(buildConfig, targets);
    }

    private static void applyAdditionalJavaImportPackages(IDLClassGeneratorParser parser, BuildToolOptions op) {
        String[] packages = op.getAdditionalJavaImportPackages();
        for(int i = 0; i < packages.length; i++) {
            parser.addAdditionalJavaImportPackage(packages[i]);
        }
    }

    private static void addTeaVMCDefaultInclude(BuildToolOptions op, TeaVMCGenerator teaVMCGenerator) {
        String includeName = findCustomInclude(op, "CustomCode.h");
        if(includeName == null) {
            includeName = findCustomInclude(op, "IDLCustomCode.h");
        }
        if(includeName != null) {
            teaVMCGenerator.addNativeCode((com.github.javaparser.ast.Node)null, "#include \"" + includeName + "\"");
        }
    }

    private static String findCustomInclude(BuildToolOptions op, String includeName) {
        File includeFile = new File(op.getCustomSourceDir(), includeName);
        if(includeFile.exists()) {
            return includeName;
        }
        return null;
    }

    private static void applyAutoGenerateFlags(BuildToolOptions op) {
        if(op.containsArg("gen_ffm")) {
            op.generateFFM = true;
        }

        if(op.containsArg("gen_jni")) {
            op.generateJNI = true;
        }

        if(op.containsArg("gen_web")) {
            op.generateWeb = true;
        }

        if(op.containsArg("gen_teavm_c")) {
            op.generateTeaVMC = true;
        }
    }

}
