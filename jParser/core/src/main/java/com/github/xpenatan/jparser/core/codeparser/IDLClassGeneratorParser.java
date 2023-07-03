package com.github.xpenatan.jparser.core.codeparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    protected final IDLReader idlReader;
    private String basePackage;

    public IDLClassGeneratorParser(String basePackage, String headerCMD, IDLReader idlReader) {
        super(headerCMD);
        this.basePackage = basePackage;
        this.idlReader = idlReader;
    }

    @Override
    public void onParseStart(JParser jParser) {
        // Generate class if it does not exist

        String packagePath = File.separator + basePackage.replace(".", File.separator);
        String basePath = new File(jParser.genDir + packagePath).getAbsolutePath();

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
}