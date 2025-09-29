package com.github.xpenatan.jparser.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author xpenatan
 */
public class JParserHelper {

    public static boolean isType(Type type, String typeStr) {
        if(type.isPrimitiveType()) {
            PrimitiveType primitiveType = type.asPrimitiveType();
            String name = primitiveType.getType().name();
            return name.contains(typeStr.toUpperCase());
        }
        else if(type.isArrayType()) {
            ArrayType arrayType = type.asArrayType();
            String name = arrayType.getElementType().toString();
            return name.contains(typeStr);
        }
        return false;
    }

    public static boolean isLong(Type type) {
        return JParserHelper.isType(type, "long");
    }

    public static boolean isInt(Type type) {
        return JParserHelper.isType(type, "int");
    }

    public static boolean isShort(Type type) {
        return JParserHelper.isType(type, "short");
    }

    public static boolean isFloat(Type type) {
        return JParserHelper.isType(type, "float");
    }

    public static boolean isDouble(Type type) {
        return JParserHelper.isType(type, "double");
    }

    public static boolean isBoolean(Type type) {
        return JParserHelper.isType(type, "boolean");
    }

    public static boolean containsMethod(ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration) {
        String nameAsString = methodDeclaration.getNameAsString();
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        List<MethodDeclaration> methodsByName = getMethodsByName(classDeclaration, nameAsString);
        for(int i = 0; i < methodsByName.size(); i++) {
            MethodDeclaration otherMethod = methodsByName.get(i);
            NodeList<Parameter> otherParameters = otherMethod.getParameters();
            boolean containsParameters = containsParameters(parameters, otherParameters);
            if(containsParameters) {
                return true;
            }
        }
        return false;
    }

    public static List<MethodDeclaration> getMethodsByName(ClassOrInterfaceDeclaration classDeclaration, String methodName) {
        ArrayList<MethodDeclaration> list = new ArrayList<>();
        List<MethodDeclaration> methods = classDeclaration.getMethods();
        int size = methods.size();
        for(int i = 0; i < size; i++) {
            MethodDeclaration methodDeclaration = methods.get(i);
            String method = methodDeclaration.getNameAsString().toLowerCase();
            String methodToCheck = methodName.toLowerCase();
            if(method.equals(methodToCheck)) {
                list.add(methodDeclaration);
            }
        }
        return list;
    }

    public static boolean containsParameters(NodeList<Parameter> parameters, NodeList<Parameter> otherParameters) {
        if(parameters.size() == otherParameters.size()) {
            boolean isValid = true;
            for(int j = 0; j < otherParameters.size(); j++) {
                Type type = parameters.get(j).getType();
                Type otherType = otherParameters.get(j).getType();
                boolean equals = type.equals(otherType);
                if(!equals) {
                    isValid = false;
                    break;
                }
            }
            return isValid;
        }
        return false;
    }

    public static boolean containsImport(CompilationUnit unit, String classPath, boolean useEqual) {
        NodeList<ImportDeclaration> imports = unit.getImports();
        for(int i = 0; i < imports.size(); i++) {
            ImportDeclaration importDeclaration = imports.get(i);
            String importName = importDeclaration.getName().asString();
            if(useEqual) {
                if(classPath.equals(importName)) {
                    return true;
                }
            }
            else {
                if(importName.contains(classPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeImport(CompilationUnit unit, String importToRemove) {
        for(ImportDeclaration anImport : unit.getImports()) {
            String nameAsString = anImport.getNameAsString();
            if(nameAsString.contains(importToRemove)) {
                unit.remove(anImport);
                break;
            }
        }
    }

    public static boolean addMissingImportType(JParser jParser, CompilationUnit unit, Type type) {
        String typeStr = type.asString();
        return addMissingImportType(jParser, unit, typeStr);
    }

    public static boolean addMissingImportType(JParser jParser, CompilationUnit unit, String className) {
        JParserItem parserUnitItem = jParser.getParserUnitItem(className);
        if(parserUnitItem != null) {
            Optional<String> fullyQualifiedName = null;
            ClassOrInterfaceDeclaration classDeclaration = parserUnitItem.getClassDeclaration();
            if(classDeclaration != null) {
                fullyQualifiedName = classDeclaration.getFullyQualifiedName();
            }
            else {
                EnumDeclaration enumDeclaration = parserUnitItem.getEnumDeclaration();
                if(enumDeclaration != null) {
                    fullyQualifiedName = enumDeclaration.getFullyQualifiedName();
                }
            }
            if(fullyQualifiedName != null && fullyQualifiedName.isPresent()) {
                String name = fullyQualifiedName.get();
                if(!JParserHelper.containsImport(unit, name, true)) {
                    unit.addImport(name);
                    return true;
                }
            }
        }
        return false;
    }

    public static ClassOrInterfaceDeclaration getClassDeclaration(CompilationUnit unit) {
        Optional<ClassOrInterfaceDeclaration> first = unit.findFirst(ClassOrInterfaceDeclaration.class);
        return first.orElse(null);
    }

    public static EnumDeclaration getEnumDeclaration(CompilationUnit unit) {
        Optional<EnumDeclaration> first = unit.findFirst(EnumDeclaration.class);
        return first.orElse(null);
    }

    public static void addMissingImportType(CompilationUnit unit, String importName) {
        boolean found = false;
        NodeList<ImportDeclaration> imports = unit.getImports();
        for(int i = 0; i < imports.size(); i++) {
            ImportDeclaration importDeclaration = imports.get(i);
            Name name = importDeclaration.getName();
            String s = name.asString();
            if(importName.equals(s)) {
                found = true;
                break;
            }
        }
        if(!found) {
            unit.addImport(importName);
        }
    }
}