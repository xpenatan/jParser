package com.github.xpenatan.jparser.core.codeparser.idl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;
import java.io.IOException;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    public boolean generateClass = false;

    protected final IDLReader idlReader;
    private String basePackage;

    protected final CompilationUnit baseClassUnit;

    private static String BASE_CLASS_NAME = "IDLBase";

    public IDLClassGeneratorParser(String basePackage, String headerCMD, IDLReader idlReader) {
        super(headerCMD);
        this.basePackage = basePackage;
        this.idlReader = idlReader;
        try {
            baseClassUnit = StaticJavaParser.parseResource("classes/" + BASE_CLASS_NAME + ".java");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onParseStart(JParser jParser) {
        if(!generateClass) {
            return;
        }

        // Generate class if it does not exist
        String packagePath = File.separator + basePackage.replace(".", File.separator);
        String basePath = new File(jParser.genDir + packagePath).getAbsolutePath();

        String baseClassPath = basePath + File.separator + BASE_CLASS_NAME + ".java";
        baseClassUnit.setPackageDeclaration(basePackage);
        jParser.unitArray.add(new JParserItem(baseClassUnit, baseClassPath, baseClassPath));

        for(IDLFile idlFile : idlReader.fileArray) {
            for(IDLClass idlClass : idlFile.classArray) {
                String className = idlClass.name;
                JParserItem parserItem = jParser.getParserUnitItem(className);
                if(parserItem == null) {
                    String classPath = basePath + File.separator + className + ".java";
                    CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(classPath);
                    CompilationUnit compilationUnit = setupClass(idlClass);
                    String code = compilationUnit.toString();
                    fileDescriptor.writeString(code, false);

                    JParserItem item = new JParserItem(compilationUnit, classPath, classPath);
                    jParser.unitArray.add(item);
                }
            }
        }
    }

    private CompilationUnit setupClass(IDLClass idlClass) {
        String className = idlClass.name;
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(basePackage);
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(className);
        classDeclaration.setPublic(true);

        if(idlClass.classHeader.isNoDelete) {
            classDeclaration.addConstructor(Modifier.Keyword.PROTECTED);
        }

        return compilationUnit;
    }

    @Override
    public void onParseClassEnd(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if(!generateClass) {
            return;
        }

        String className = classOrInterfaceDeclaration.getName().toString();
        IDLClass idlClass = idlReader.getClass(className);
        if(idlClass != null) {
            if(!idlClass.extendClass.isEmpty()) {
                JParserItem parentItem = jParser.getParserUnitItem(idlClass.extendClass);
                if(parentItem != null) {
                    CompilationUnit parentUnit = parentItem.unit;
                    if(parentUnit.getPackageDeclaration().isPresent()) {
                        String importName = parentUnit.getPackageDeclaration().get().getNameAsString() + "." + idlClass.extendClass;
                        unit.addImport(importName);
                    }
                    classOrInterfaceDeclaration.addExtendedType(idlClass.extendClass);
                }
            }
            else {
                if(baseClassUnit.getPackageDeclaration().isPresent()) {
                    String importName = baseClassUnit.getPackageDeclaration().get().getNameAsString() + "." + BASE_CLASS_NAME;
                    unit.addImport(importName);
                }
                classOrInterfaceDeclaration.addExtendedType(BASE_CLASS_NAME);
            }
        }
    }
}