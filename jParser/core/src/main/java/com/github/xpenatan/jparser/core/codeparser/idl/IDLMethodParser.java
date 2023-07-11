package com.github.xpenatan.jparser.core.codeparser.idl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IDLMethodParser {

    public static void generateMethods(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLMethod idlMethod) {
        if(idlMethod.skip) {
            return;
        }

        String methodName = idlMethod.name;

        MethodDeclaration containsMethod = containsMethod(classOrInterfaceDeclaration, idlMethod);
        if(containsMethod != null) {
            boolean isNative = containsMethod.isNative();
            boolean isStatic = containsMethod.isStatic();
            boolean containsBlockComment = false;
            Optional<Comment> optionalComment = containsMethod.getComment();
            if(optionalComment.isPresent()) {
                Comment comment = optionalComment.get();
                if(comment instanceof BlockComment) {
                    BlockComment blockComment = (BlockComment)optionalComment.get();
                    String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                    // Skip if method already exist with header code
                    if(headerCommands != null) {
                        if(headerCommands.contains(DefaultCodeParser.CMD_NATIVE)) {
                            return;
                        }
                        else {
                            if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                                //If skip is found then remove the method
                                containsMethod.remove();
                            }
                            return;
                        }
                    }
                }
            }
            if(isNative) {
                // It's a dummy method. Remove it and let IDL generate it again
                containsMethod.remove();
            }
            if(!isNative && !isStatic) {
                // if a simple method exist, keep it and don't let IDL generate the method.
                return;
            }
        }

        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        MethodDeclaration methodDeclaration = classOrInterfaceDeclaration.addMethod(methodName, Modifier.Keyword.PUBLIC);
        methodDeclaration.setStatic(idlMethod.isStaticMethod);
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter idlParameter = parameters.get(i);
            String paramType = idlParameter.type;
            String paramName = idlParameter.name;
            Parameter parameter = methodDeclaration.addAndGetParameter(paramType, paramName);
            Type type = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, type);
        }

        Type returnType = StaticJavaParser.parseType(idlMethod.returnType);
        methodDeclaration.setType(returnType);
        IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, returnType, methodDeclaration);
        idlParser.onIDLMethodGenerated(jParser, idlClass, idlMethod,  unit, classOrInterfaceDeclaration, methodDeclaration, false);
    }

    private static MethodDeclaration containsMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLMethod idlMethod) {
        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        String[] paramTypes = new String[parameters.size()];

        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.type;
            paramTypes[i] = paramType;
        }
        List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethodsBySignature(idlMethod.name, paramTypes);

        if(methods.size() > 0) {
            return methods.get(0);
        }
        return null;
    }

}
