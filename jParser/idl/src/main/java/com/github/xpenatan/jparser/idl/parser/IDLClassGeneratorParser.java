package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.core.util.CustomPrettyPrinter;
import com.github.xpenatan.jparser.core.util.FileHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLClassOrEnum;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.ResourceList;
import idl.IDLBase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    public boolean generateClass = false;

    protected final IDLReader idlReader;
    private String basePackage;

    protected CompilationUnit baseClassUnit;
    protected CompilationUnit enumClassUnit;

    private static String BASE_CLASS_NAME = "-";
    private static String IDLENUM_CLASS_NAME = "-";

    protected HashMap<String, String> classCppPath;

    public IDLClassGeneratorParser(String basePackage, String headerCMD, IDLReader idlReader) {
        super(headerCMD);
        this.basePackage = basePackage;
        this.idlReader = idlReader;
    }

    @Override
    public void onParseStart(JParser jParser) {
        if(!generateClass) {
            return;
        }

        String baseIDLPath = "IDLHelper.idl";
        InputStream resourceAsStream = IDLBase.class.getClassLoader().getResourceAsStream(baseIDLPath);
        InputStreamReader streamReader = new InputStreamReader(resourceAsStream);
        IDLFile baseIDLFile = IDLReader.parseFile(streamReader);
        idlReader.fileArray.add(baseIDLFile);

        createBaseUnitFromResources(jParser);
        generateIDLJavaClasses(jParser, jParser.genDir);
    }

    private void createBaseUnitFromResources(JParser jParser) {
        BASE_CLASS_NAME = IDLBase.class.getSimpleName();
        IDLENUM_CLASS_NAME = IDLEnum.class.getSimpleName();
        Collection<String> resources = ResourceList.getResources(Pattern.compile("/*.*/*.java"));
        for(String resource : resources) {
            try {
                CompilationUnit compilationUnit = StaticJavaParser.parseResource(resource);
                compilationUnit.printer(new CustomPrettyPrinter());
                String originalPackage = compilationUnit.getPackageDeclaration().get().getNameAsString();
                if(basePackage != null && !basePackage.isEmpty()) {
                    originalPackage = "." + originalPackage;
                }
                String newPackage = basePackage + originalPackage;
                compilationUnit.setPackageDeclaration(newPackage);
                jParser.unitArray.add(new JParserItem(compilationUnit, jParser.genDir, jParser.genDir));
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        JParserItem parserUnitItem = jParser.getParserUnitItem(BASE_CLASS_NAME);
        baseClassUnit = parserUnitItem.unit;

        JParserItem parserEnumItem = jParser.getParserUnitItem(IDLENUM_CLASS_NAME);
        enumClassUnit = parserEnumItem.unit;
    }

    private void generateIDLJavaClasses(JParser jParser, String genPath) {
        classCppPath = getClassCppPath();
        for(IDLFile idlFile : idlReader.fileArray) {
            for(IDLClassOrEnum idlClassOrEnum : idlFile.classArray) {
                String className = idlClassOrEnum.name;
                JParserItem parserItem = jParser.getParserUnitItem(className);
                if(parserItem == null) {
                    if(idlClassOrEnum.isClass()) {
                        String jsImplementation = idlClassOrEnum.asClass().classHeader.jsImplementation;
                        if(jsImplementation != null) {
                            //Don't generate class if its js implementation
                            continue;
                        }
                    }

                    String subPackage = "";
                    if(classCppPath.containsKey(className)) {
                        String includeClass = classCppPath.get(className);
                        Path p = Paths.get(includeClass);
                        Path parent = p.getParent();
                        if(parent != null) {
                            String string = parent.toString();
                            subPackage = string.replace(File.separator, ".").toLowerCase();
                        }
                    }
                    CompilationUnit compilationUnit = setupClass(idlClassOrEnum, subPackage);
                    parserItem = new JParserItem(compilationUnit, genPath, genPath);
                    jParser.unitArray.add(parserItem);
                }
            }
        }
    }

    private HashMap<String, String> getClassCppPath() {
        HashMap<String, String> mapPackage = new HashMap<>();
        String cppDir = idlReader.cppDir;
        if(cppDir != null) {
            ArrayList<String> filesFromDir = FileHelper.getFilesFromDir(cppDir);
            for(String path : filesFromDir) {
                if(!path.endsWith(".h"))
                    continue;
                String include = path.replace(cppDir, "");
                String out = include.replace(".h", "");

                Path p = Paths.get(out);
                String className = p.getFileName().toString();
                mapPackage.put(className, include);
            }
        }
        return mapPackage;
    }

    private CompilationUnit setupClass(IDLClassOrEnum idlClass, String subPackage) {
        String className = idlClass.name;
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.printer(new CustomPrettyPrinter());
        if(subPackage != null && !subPackage.isEmpty()) {
            subPackage = "." + subPackage;
        }
        compilationUnit.setPackageDeclaration(basePackage + subPackage);
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(className);
        classDeclaration.setPublic(true);

        if(idlClass.isClass()) {
            if(idlClass.asClass().classHeader.isNoDelete) {
                // Class with no delete don't have constructor
                classDeclaration.addConstructor(Modifier.Keyword.PROTECTED);
            }
        }

        return compilationUnit;
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if(!generateClass) {
            return;
        }

        String className = classOrInterfaceDeclaration.getName().toString();

        IDLEnum idlEnum = idlReader.getEnum(className);
        if(idlEnum != null) {
            JParserItem parentItem = jParser.getParserUnitItem(IDLENUM_CLASS_NAME);
            if(parentItem != null) {
                ClassOrInterfaceDeclaration classDeclaration = parentItem.getClassDeclaration();
                if(classDeclaration != null) {
                    String importName = enumClassUnit.getPackageDeclaration().get().getNameAsString() + "." + IDLENUM_CLASS_NAME;
                    unit.addImport(importName);
                    if(classOrInterfaceDeclaration.getImplementedTypes().isEmpty()) {
                        classOrInterfaceDeclaration.addImplementedType(IDLENUM_CLASS_NAME);
                    }
                }
            }
        }

        IDLClass idlClass = idlReader.getClass(className);
        if(idlClass != null) {
            if(!idlClass.extendClass.isEmpty()) {
                JParserItem parentItem = jParser.getParserUnitItem(idlClass.extendClass);
                if(parentItem != null) {
                    ClassOrInterfaceDeclaration classDeclaration = parentItem.getClassDeclaration();
                    if(classDeclaration != null) {
                        if(classOrInterfaceDeclaration.getExtendedTypes().isEmpty()) {
                            CompilationUnit parentUnit = parentItem.unit;
                            if(parentUnit.getPackageDeclaration().isPresent()) {
                                String importName = parentUnit.getPackageDeclaration().get().getNameAsString() + "." + idlClass.extendClass;
                                unit.addImport(importName);
                            }
                            classOrInterfaceDeclaration.addExtendedType(idlClass.extendClass);
                        }
                    }
                }
            }
            else {
                JParserItem parentItem = jParser.getParserUnitItem(BASE_CLASS_NAME);
                if(parentItem != null) {
                    ClassOrInterfaceDeclaration classDeclaration = parentItem.getClassDeclaration();
                    if(classDeclaration != null) {
                        for(ImportDeclaration anImport : unit.getImports()) {
                            String nameAsString = anImport.getNameAsString();
                            if(nameAsString.contains(BASE_CLASS_NAME)) {
                                unit.remove(anImport);
                                break;
                            }
                        }
                        String importName = baseClassUnit.getPackageDeclaration().get().getNameAsString() + "." + BASE_CLASS_NAME;
                        unit.addImport(importName);
                        if(classOrInterfaceDeclaration.getExtendedTypes().isEmpty()) {
                            classOrInterfaceDeclaration.addExtendedType(BASE_CLASS_NAME);
                        }
                    }
                }
            }
        }
    }
}