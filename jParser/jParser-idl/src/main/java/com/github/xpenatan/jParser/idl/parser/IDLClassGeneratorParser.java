package com.github.xpenatan.jParser.idl.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jParser.idl.IDLBase;
import com.github.xpenatan.jParser.idl.IDLEnum;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserHelper;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jParser.core.util.CustomPrettyPrinter;
import com.github.xpenatan.jParser.core.util.FileHelper;
import com.github.xpenatan.jParser.core.util.ResourceList;
import com.github.xpenatan.jParser.idl.IDLClass;
import com.github.xpenatan.jParser.idl.IDLClassOrEnum;
import com.github.xpenatan.jParser.idl.IDLEnumClass;
import com.github.xpenatan.jParser.idl.IDLFile;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.IDLRenaming;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author xpenatan
 */
public abstract class IDLClassGeneratorParser extends DefaultCodeParser {

    public boolean generateClass = false;

    protected final IDLReader idlReader;
    protected String basePackage;

    protected HashMap<String, String> classCppPath;

    protected String includeDir;

    public IDLRenaming idlRenaming;

    /**
     * @param basePackage Base module source. This is used to generate other sources
     * @param headerCMD   This is the first command option that this parser will use. Ex teavm, C++.
     * @param idlReader   Contains the parsed idl files
     * @param includeDir  This is used to add java subpackages from c++ tree. Without this all java source will be at the root package.
     */
    public IDLClassGeneratorParser(String basePackage, String headerCMD, IDLReader idlReader, String includeDir) {
        super(headerCMD);
        this.includeDir = includeDir;
        this.basePackage = basePackage;
        this.idlReader = idlReader;
        if(this.includeDir != null) {
            this.includeDir = this.includeDir.replace("\\", "/").replace("//", "/");
        }
    }

    @Override
    public void onParseStart(JParser jParser) {
        JParserItem baseItem = new JParserItem(null, null);
        Class<IDLBase> idlBaseClass = IDLBase.class;
        baseItem.packagePathName = idlBaseClass.getPackageName();
        baseItem.className = idlBaseClass.getSimpleName();
        baseItem.notAllowed = true;
        baseItem.isIDL = true; // Make it be used to generate methods that use this class
        jParser.unitArray.add(baseItem);

        if(!generateClass) {
            return;
        }

        classCppPath = getClassCppPath();
        createBaseUnitFromResources(jParser);
        generateIDLJavaClasses(jParser, jParser.genDir);
    }

    protected String getUpdatePackage(CompilationUnit compilationUnit) {
        String originalPackage = "";
        if(basePackage != null && !basePackage.isEmpty() && compilationUnit.getPackageDeclaration().isPresent()) {
            originalPackage = compilationUnit.getPackageDeclaration().get().getNameAsString();
        }
        if(originalPackage.isEmpty()) {
            return basePackage;
        }
        if(originalPackage.startsWith(basePackage)) {
            return originalPackage;
        }
        String lastPart = basePackage.substring(basePackage.lastIndexOf('.') + 1);
        if(originalPackage.equals(lastPart)) {
            return basePackage;
        }
        if(originalPackage.startsWith(lastPart + ".")) {
            return basePackage + originalPackage.substring(lastPart.length());
        }
        return basePackage + "." + originalPackage;
    }

    private void createBaseUnitFromResources(JParser jParser) {
        Collection<String> resources = ResourceList.getResources(Pattern.compile("/*.*/*.java"));
        for(String resource : resources) {
            try {
                CompilationUnit compilationUnit = StaticJavaParser.parseResource(resource);

                Optional<ClassOrInterfaceDeclaration> classOrInterfaceDeclaration = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class);
                if(classOrInterfaceDeclaration.isPresent()) {
                    String nameAsString = classOrInterfaceDeclaration.get().getNameAsString();
                    JParserItem jParserItem = jParser.getParserUnitItem(nameAsString);
                    if(jParserItem != null) {
                        compilationUnit =jParserItem.unit;
                    }
                    else {
                        compilationUnit.printer(new CustomPrettyPrinter());
                        jParserItem = new JParserItem(compilationUnit, jParser.genDir);
                        jParser.unitArray.add(jParserItem);
                    }
                    String newPackage = getUpdatePackage(compilationUnit);
                    compilationUnit.setPackageDeclaration(newPackage);
                    if(!JParser.CREATE_IDL_HELPER) {
                        jParserItem.notAllowed = true;
                    }
                    jParserItem.isIDL = true;
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void generateIDLJavaClasses(JParser jParser, String genPath) {
        System.out.println("Class Mapping: ");
        System.out.println(classCppPath);

        for(IDLFile idlFile : idlReader.fileArray) {
            if(idlFile.skip) {
                continue;
            }
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

                    if(idlClassOrEnum.idlSkip) {
                        continue;
                    }

                    String subPackage = "";
                    if(classCppPath.containsKey(className)) {
                        String includeClass = classCppPath.get(className);
                        Path p = Paths.get(includeClass);
                        Path parent = p.getParent();
                        if(parent != null) {
                            String string = parent.toString().replace("\\", "/");
                            subPackage = string.replace("/", ".").toLowerCase();
                        }
                    }
                    if(idlClassOrEnum.subPackage != null) {
                        subPackage = idlClassOrEnum.subPackage;
                    }
                    if(idlRenaming != null) {
                        subPackage = idlRenaming.obtainNewPackage(idlClassOrEnum, subPackage);
                    }
                    CompilationUnit compilationUnit = setupClass(idlClassOrEnum, subPackage);
                    parserItem = new JParserItem(compilationUnit, genPath);
                    jParser.unitArray.add(parserItem);
                }
            }
        }
    }

    private HashMap<String, String> getClassCppPath() {
        HashMap<String, String> mapPackage = new HashMap<>();
        System.out.println("includeDir: " + includeDir);
        if(includeDir != null) {
            ArrayList<String> filesFromDir = FileHelper.getFilesFromDir(includeDir);
            System.out.println("FilesFromDir: " + filesFromDir.size());
            for(String path : filesFromDir) {
                if(!path.endsWith(".h"))
                    continue;
                path = path.replace("\\", "/").replace("//", "/");
                String include = path.replace(includeDir, "");
                if(include.startsWith("/")) {
                    include = include.replaceFirst("/", "");
                }
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
        if(subPackage != null && !subPackage.isEmpty() && !subPackage.startsWith(".")) {
            subPackage = "." + subPackage;
        }
        compilationUnit.setPackageDeclaration(basePackage + subPackage);
        if(idlClass.isClass()) {
            ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(className);
            classDeclaration.setPublic(true);

            IDLClass aClass = idlClass.asClass();
            if(aClass.isCallback) {
                // Do nothing
            }
            else {
                // For every class we generate empty object that can be used when needed.
                IDLMethodParser.generateFieldName("NULL", classDeclaration, className, true, Modifier.Keyword.PUBLIC, true);
            }
        }
        else if(idlClass.isEnum()) {
            EnumDeclaration enumDeclaration = compilationUnit.addEnum(className);
            EnumConstantDeclaration enumConstantDeclaration = enumDeclaration.addEnumConstant("CUSTOM");
            enumConstantDeclaration.addArgument("0");

            Type intType = StaticJavaParser.parseType(int.class.getSimpleName());
            enumDeclaration.addField(intType, "value", Modifier.Keyword.PRIVATE);
            ConstructorDeclaration constructorDeclaration = enumDeclaration.addConstructor(Modifier.Keyword.PRIVATE);
            constructorDeclaration.addParameter(intType, "value");
            constructorDeclaration.getBody().addStatement("this.value = value;");

            MethodDeclaration getMethodDeclaration = enumDeclaration.addMethod("getValue", Modifier.Keyword.PUBLIC);
            getMethodDeclaration.setType(intType);
            getMethodDeclaration.getBody().get().addStatement("return value;");

            MethodDeclaration setMethodDeclaration = enumDeclaration.addMethod("setValue", Modifier.Keyword.PUBLIC);
            setMethodDeclaration.addParameter(intType, "value");
            setMethodDeclaration.setType(className);
            BlockStmt blockStmt = setMethodDeclaration.getBody().get();
            blockStmt.addStatement(
                    "if(this != CUSTOM) {" +
                        "throw new RuntimeException(\"Cannot change none CUSTOM value\"); " +
                    "}");
            blockStmt.addStatement("this.value = value;");
            blockStmt.addStatement("return this;");

            MethodDeclaration getCustomMethodDeclaration = enumDeclaration.addMethod("getCustom", Modifier.Keyword.PUBLIC);
            getCustomMethodDeclaration.setType(className);
            getCustomMethodDeclaration.getBody().get().addStatement("return CUSTOM;");
        }
        // Hack to inject internal dependencies
        return StaticJavaParser.parse(compilationUnit.toString());
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum) {
        if(!generateClass) {
            return;
        }

        String className = classOrEnum.getName().toString();

        IDLEnumClass idlEnum = idlReader.getEnum(className);
        if(idlEnum != null) {
            EnumDeclaration enumDeclaration = (EnumDeclaration)classOrEnum;
//            JParserItem parentItem = jParser.getParserUnitItem(IDLENUM_CLASS_NAME);
//            if(parentItem != null) {
//                ClassOrInterfaceDeclaration classDeclaration = parentItem.getClassDeclaration();
//                if(classDeclaration != null) {

            Class<IDLEnum> idlEnumClass = IDLEnum.class;
            String simpleName = idlEnumClass.getSimpleName();
            JParserHelper.removeImport(unit, simpleName);
            String importName = idlEnumClass.getPackageName() + "." + simpleName;
            unit.addImport(importName);
            if(enumDeclaration.getImplementedTypes().isEmpty()) {
                enumDeclaration.addImplementedType(simpleName + "<" + className + ">");
            }
//                }
//            }
        }

        IDLClass idlClass = idlReader.getClass(className);
        if(idlClass != null) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)classOrEnum;
            if(!idlClass.extendClass.isEmpty()) {
                JParserItem parentItem = jParser.getParserUnitItem(idlClass.extendClass);
                if(parentItem != null) {
                    ClassOrInterfaceDeclaration classDeclaration = parentItem.getClassDeclaration();
                    if(classDeclaration != null) {
//                        JParserHelper.removeImport(unit, BASE_CLASS_NAME);
//                        NodeList<ClassOrInterfaceType> extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
//                        if(!extendedTypes.isEmpty() && extendedTypes.get(0).getNameAsString().contains(BASE_CLASS_NAME)) {
//                            String importName = baseClassUnit.getPackageDeclaration().get().getNameAsString() + "." + BASE_CLASS_NAME;
//                            unit.addImport(importName);
//                        }

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
                String name = IDL_BASE_CLASS;
                JParserItem parentItem = jParser.getParserUnitItem(name);
                if(parentItem != null) {
                    JParserHelper.removeImport(unit, name);
                    String importName = parentItem.packagePathName + "." + name;
                    unit.addImport(importName);
                    if(classOrInterfaceDeclaration.getExtendedTypes().isEmpty()) {
                        classOrInterfaceDeclaration.addExtendedType(name);
                    }
                }
            }
        }
    }

    @Override
    public void onParserComplete(JParser jParser, ArrayList<JParserItem> parserItems) {
        // Fix existing IDL imports if it's used in base.

        for(int i = 0; i < parserItems.size(); i++) {
            JParserItem parserItem = parserItems.get(i);
            CompilationUnit unit = parserItem.unit;

            // Add missing imports
            ClassOrInterfaceDeclaration classDeclaration = parserItem.getClassDeclaration();
            if(classDeclaration != null) {
                // add import by looking into fields and methods
                List<FieldDeclaration> fields = classDeclaration.getFields();
                for(FieldDeclaration field : fields) {
                    Type elementType = field.getElementType();
                    addImport(jParser, unit, elementType);
                }

                List<MethodDeclaration> methods = classDeclaration.getMethods();
                for(MethodDeclaration method : methods) {
                    Type returnType = method.getType();
                    addImport(jParser, unit, returnType);
                    NodeList<Parameter> parameters = method.getParameters();
                    for(Parameter parameter : parameters) {
                        Type type = parameter.getType();
                        addImport(jParser, unit, type);
                    }
                }
            }

            {
                // Update imports if its invalid.
                NodeList<ImportDeclaration> imports = unit.getImports();
                ArrayList<ImportDeclaration> importsToRemove = new ArrayList<>();
                for(ImportDeclaration anImport : imports) {
                    Name name = anImport.getName();
                    String identifier = name.getIdentifier();
                    JParserItem parserUnitItem = jParser.getParserUnitItem(identifier);
                    if(parserUnitItem != null) {
                        importsToRemove.add(anImport);
                    }
                }

                for(ImportDeclaration anImport : importsToRemove) {
                    Name name = anImport.getName();
                    String identifier = name.getIdentifier();
                    JParserItem parserUnitItem = jParser.getParserUnitItem(identifier);
                    if(parserUnitItem != null) {
                        CompilationUnit importUnit = parserUnitItem.unit;
                        if(importUnit == null) {
                            continue;
                        }
                        anImport.remove();

                        boolean skipUnit = false;

                        if(!JParser.CREATE_IDL_HELPER) {
                            //TODO implement better class renaming
                            // Hack to look for idl classes that was generated with the main lib
                            ArrayList<String> baseIDLClasses = getBaseIDLClasses();
                            for(String baseIDLClass : baseIDLClasses) {
                                String[] split = baseIDLClass.split("\\.");
                                String s = split[split.length - 1];
                                if(s.equals(identifier)) {
                                    unit.addImport(baseIDLClass);
                                    skipUnit = true;
                                    break;
                                }
                            }
                        }

                        if(!skipUnit) {
                            String importName = importUnit.getPackageDeclaration().get().getNameAsString() + "." + parserUnitItem.className;
                            unit.addImport(importName);
                        }
                    }
                }
            }
        }

    }

    private static void addImport(JParser jParser, CompilationUnit unit, Type elementType) {
        if(elementType.isClassOrInterfaceType()) {
            if(!JParserHelper.addMissingImportType(jParser, unit, elementType)) {
                // class type not found. Try to get from resources.
                String typeStr = elementType.asString();
                Collection<String> resources = ResourceList.getResources(Pattern.compile("/*.*/" + typeStr + ".class"));
                for(String resource : resources) {
                    resource = resource.replace("/", ".").replace(".class", "");
                    if(!JParserHelper.containsImport(unit, typeStr, false)) {
                        unit.addImport(resource);
                    }
                }
            }
        }
    }

    public static ArrayList<String> getBaseIDLClasses() {
        ArrayList<String> classes = new ArrayList<>();
        Collection<String> resources = ResourceList.getResources(Pattern.compile("/*.*/*.java"));

        for(String resource : resources) {
            resource = resource.replace("/", ".");
            String classNameAndPath = resource.replace(".java", "");
            String[] split = classNameAndPath.split("/.");
            String name = split[split.length - 1];
            Collection<String> compiledClass = ResourceList.getResources(Pattern.compile("/*.*/" + name + ".class"));
            if(compiledClass != null) {
                for(String aClass : compiledClass) {
                    aClass = aClass.replace("/", ".").replace(".class", "");
                    if(!aClass.startsWith(classNameAndPath)) {
                        classes.add(aClass);
                        break;
                    }
                }
            }
            else {
                classes.add(classNameAndPath);
            }
        }
        return classes;
    }
}

