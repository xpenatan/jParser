package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import java.util.ArrayList;

public class IDLCallbackParser {

    public static void generateCallback(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass) {

        ArrayList<IDLConstructor> constructors = idlClass.callback.constructors;
        for(int i = 0; i < constructors.size(); i++) {
            IDLConstructor idlConstructor = constructors.get(i);
            ConstructorDeclaration constructorDeclaration = IDLConstructorParser.getOrCreateConstructorDeclaration(idlParser, jParser, unit, classOrInterfaceDeclaration, idlConstructor);

            if(constructorDeclaration.getBody().isEmpty()) {
                MethodDeclaration nativeMethod = IDLConstructorParser.setupConstructor(idlConstructor, classOrInterfaceDeclaration, constructorDeclaration);
                idlParser.onIDLConstructorGenerated(jParser, idlConstructor, classOrInterfaceDeclaration, constructorDeclaration, nativeMethod);
            }
        }
    }
}