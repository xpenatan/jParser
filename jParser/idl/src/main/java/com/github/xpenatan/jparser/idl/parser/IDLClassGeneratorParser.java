package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.core.util.CustomPrettyPrinter;
import com.github.xpenatan.jparser.core.util.FileHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLClassOrEnum;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.core.util.ResourceList;
import com.github.xpenatan.jparser.idl.IDLPackageRenaming;
import idl.IDLBase;
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
    private String basePackage;

    protected CompilationUnit baseClassUnit;
    protected CompilationUnit enumClassUnit;

    private static String BASE_CLASS_NAME = "-";
    private static String IDLENUM_CLASS_NAME = "-";

    protected HashMap<String, String> classCppPath;

    protected String includeDir;

    public IDLPackageRenaming packageRenaming;

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
        if(!generateClass) {
            return;
        }
        classCppPath = getClassCppPath();
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
                JParserItem jParserItem = new JParserItem(compilationUnit, jParser.genDir);
                if(!JParser.CREATE_IDL_HELPER) {
                    jParserItem.notAllowed = true;
                }
                jParserItem.isIDL = true;
                jParser.unitArray.add(jParserItem);
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
        System.out.println("Class Mapping: ");
        System.out.println(classCppPath);

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
                            String string = parent.toString().replace("\\", "/");
                            subPackage = string.replace("/", ".").toLowerCase();
                            if(packageRenaming != null) {
                                subPackage = packageRenaming.obtainNewPackage(subPackage);
                            }
                        }
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
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(className);
        classDeclaration.setPublic(true);

        if(idlClass.isClass()) {
            IDLClass aClass = idlClass.asClass();
            if(aClass.isCallback) {
                // Do nothing
            }
            else {
                // For every class we generate empty object that can be used when needed.
                IDLMethodParser.generateFieldName("T_01", classDeclaration, className, true, Modifier.Keyword.PUBLIC, true);
                IDLMethodParser.generateFieldName("T_02", classDeclaration, className, true, Modifier.Keyword.PUBLIC, true);
                IDLMethodParser.generateFieldName("T_03", classDeclaration, className, true, Modifier.Keyword.PUBLIC, true);
            }
        }
        // Hack to inject internal dependencies
        return StaticJavaParser.parse(compilationUnit.toString());
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
                    JParserHelper.removeImport(unit, IDLENUM_CLASS_NAME);
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
                        JParserHelper.removeImport(unit, BASE_CLASS_NAME);
                        NodeList<ClassOrInterfaceType> extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
                        if(!extendedTypes.isEmpty() && extendedTypes.get(0).getNameAsString().contains(BASE_CLASS_NAME)) {
                            String importName = baseClassUnit.getPackageDeclaration().get().getNameAsString() + "." + BASE_CLASS_NAME;
                            unit.addImport(importName);
                        }

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
                        JParserHelper.removeImport(unit, BASE_CLASS_NAME);
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

        // Add additional method to call natives
        makeNativesPublic(parserItems);
    }

    private void makeNativesPublic(ArrayList<JParserItem> parserItems) {
        for(int i = 0; i < parserItems.size(); i++) {
            JParserItem parserItem = parserItems.get(i);
            String className = parserItem.className;
            IDLClass idlClass = idlReader.getClass(className);
            if(idlClass != null && idlClass.idlSkip) {
                continue;
            }

            CompilationUnit unit = parserItem.unit;
            List<ClassOrInterfaceDeclaration> classDeclarations = unit.findAll(ClassOrInterfaceDeclaration.class);

            for(int i1 = 0; i1 < classDeclarations.size(); i1++) {
                ClassOrInterfaceDeclaration classDeclaration = classDeclarations.get(i1);

                ArrayList<Pair<MethodDeclaration, MethodDeclaration>> pairList = new ArrayList<>();

                List<MethodCallExpr> methodCalls = classDeclaration.findAll(MethodCallExpr.class);

                for(int i2 = 0; i2 < methodCalls.size(); i2++) {
                    MethodCallExpr methodCallExpr = methodCalls.get(i2);

                    try {
                        ResolvedMethodDeclaration resolve = methodCallExpr.resolve();
                        if(resolve instanceof JavaParserMethodDeclaration) {
                            JavaParserMethodDeclaration jpmd = (JavaParserMethodDeclaration)resolve;
                            MethodDeclaration nativeMethod = jpmd.getWrappedNode();
                            if(nativeMethod.isNative()) {
                                Node node = methodCallExpr.getParentNode().get();
                                while(node != null) {
                                    if(node instanceof MethodDeclaration) {
                                        MethodDeclaration callMethod = (MethodDeclaration)node;
                                        pairList.add(new Pair<>(callMethod, nativeMethod));
                                        break;
                                    }
                                    else if(node instanceof ConstructorDeclaration) {
                                        pairList.add(new Pair<>(null, nativeMethod));
                                        break;
                                    }
                                    Optional<Node> parentNode = node.getParentNode();
                                    node = parentNode.orElse(null);
                                }
                            }
                        }
                    } catch(Throwable t) {
                    }
                }

                for(int i2 = 0; i2 < pairList.size(); i2++) {
                    Pair<MethodDeclaration, MethodDeclaration> pair = pairList.get(i2);
                    MethodDeclaration methodDeclaration = pair.a;
                    MethodDeclaration nativeMethod = pair.b;
                    duplicateNativeMethodToLong(classDeclaration, methodDeclaration, nativeMethod);
                }
            }
        }
    }

    private void duplicateNativeMethodToLong(ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        // Because all native code convert int to long. If it's called from outside the library teavm will give errors.
        // This is to duplicate all native code method to keep the long address parameters.

        MethodDeclaration clone = nativeMethod.clone();
        clone.getAnnotations().clear();
        clone.removeModifier(Modifier.Keyword.NATIVE);
        clone.removeModifier(Modifier.Keyword.PRIVATE);
        clone.removeModifier(Modifier.Keyword.STATIC);
        clone.addModifier(Modifier.Keyword.PUBLIC);
        clone.addModifier(Modifier.Keyword.STATIC);
        clone.removeComment();
        NodeList<Parameter> parameters = clone.getParameters();
        int paramSize = parameters.size();
        for(int i1 = 0; i1 < paramSize; i1++) {
            Parameter parameter = parameters.get(i1);
            String paramName = parameter.getNameAsString();
            if(paramName.endsWith("_addr")) {
                parameter.setType(PrimitiveType.longType());
            }
        }
        if(!clone.getNameAsString().contains(IDLMethodParser.NATIVE_METHOD)) {
            return;
        }
        String newName = clone.getNameAsString().replace(IDLMethodParser.NATIVE_METHOD, "native_");
        clone.setName(newName);

        String returnStr = "\t";

        if(methodDeclaration == null) {
            // Is constructor
            Type type = nativeMethod.getType();
            if(JParserHelper.isLong(type)) {
                returnStr += "return ";
                clone.setType(PrimitiveType.longType());
            }
        }
        else {
            Type type = methodDeclaration.getType();
            if(type.isClassOrInterfaceType() && !nativeMethod.isClassOrInterfaceDeclaration()) {
                returnStr += "return ";
                clone.setType(PrimitiveType.longType());
            }
            else if(!type.isVoidType()) {
                returnStr += "return ";
            }
        }
        String methodStr = getMethodStr(nativeMethod, paramSize, parameters);
        String body = "{\n";
        body += returnStr;
        body += methodStr;
        body += "\n}";

        BodyDeclaration<?> bodyDeclaration = StaticJavaParser.parseBodyDeclaration(body);
        InitializerDeclaration initializerDeclaration = (InitializerDeclaration)bodyDeclaration;
        clone.setBody(initializerDeclaration.getBody());

        NodeList<Parameter> parameters1 = clone.getParameters();
        String[] param = new String[parameters1.size()];
        for(int i = 0; i < parameters1.size(); i++) {
            Parameter parameter = parameters1.get(i);
            String nameAsString = parameter.getType().asString();
            param[i] = nameAsString;
        }
        List<MethodDeclaration> methodsBySignature = classDeclaration.getMethodsBySignature(clone.getNameAsString(), param);
        if(methodsBySignature.isEmpty()) {
            classDeclaration.addMember(clone);
        }
    }

    private static String getMethodStr(MethodDeclaration nativeMethod, int paramSize, NodeList<Parameter> parameters) {
        String methodStr = nativeMethod.getNameAsString();
        String params = "";
        for(int i1 = 0; i1 < paramSize; i1++) {
            String comma = "";
            if(i1 < paramSize - 1) {
                comma = ", ";
            }
            Parameter parameter = parameters.get(i1);
            String paramName = parameter.getNameAsString();
            params += paramName;
            params += comma;
        }

        methodStr += "(" + params + ");";
        return methodStr;
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