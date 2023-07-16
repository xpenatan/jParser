package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;
import java.util.Optional;

public class IDLConstructorParser {

    public static void generateConstructor(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLConstructor idlConstructor) {
        ConstructorDeclaration constructorDeclaration = containsConstructor(classOrInterfaceDeclaration, idlConstructor);
        if(constructorDeclaration == null) {
            constructorDeclaration = classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PUBLIC);
            ArrayList<IDLParameter> parameters = idlConstructor.parameters;
            for(int i = 0; i < parameters.size(); i++) {
                IDLParameter parameter = parameters.get(i);
                JParserHelper.addMissingImportType(jParser, unit, parameter.type);
                constructorDeclaration.addAndGetParameter(parameter.type, parameter.name);
            }
            setupConstructor(jParser, idlConstructor, constructorDeclaration);
        }
    }

    private static void setupConstructor(JParser jParser, IDLConstructor idlConstructor, ConstructorDeclaration constructorDeclaration) {

    }

    private static ConstructorDeclaration containsConstructor(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLConstructor idlConstructor) {
        ArrayList<IDLParameter> parameters = idlConstructor.parameters;
        String[] paramTypes = new String[parameters.size()];
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.type;
            paramTypes[i] = paramType;
        }
        Optional<ConstructorDeclaration> constructorDeclarationOptional = classOrInterfaceDeclaration.getConstructorByParameterTypes(paramTypes);
        return constructorDeclarationOptional.orElse(null);
    }
}
