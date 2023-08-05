package com.github.xpenatan.jparser.idl.parser;

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
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLClass;
import java.util.List;
import java.util.Optional;

public class IDLAttributeParser {

    public static void generateAttribute(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLAttribute idlAttribute) {
        if(idlAttribute.skip) {
            return;
        }
        MethodDeclaration containsSetMethod = containsSetMethod(classOrInterfaceDeclaration, idlAttribute);
        MethodDeclaration containsGetMethod = containsGetMethod(classOrInterfaceDeclaration, idlAttribute);

        String attributeName = idlAttribute.name;
        String attributeType = idlAttribute.type;

        Type type = null;
        try {
            type = StaticJavaParser.parseType(attributeType);
        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }

        JParserItem parserUnitItem = jParser.getParserUnitItem(type.toString());
        if(parserUnitItem != null) {
            if(parserUnitItem.notAllowed) {
                // skip generating set/get for class that is not allowed to have get and set. Ex Enum
                return;
            }
        }

        boolean addGet = true;
        boolean addSet = true;
        MethodDeclaration getMethodDeclaration = null;
        List<MethodDeclaration> getMethods = classOrInterfaceDeclaration.getMethodsBySignature(attributeName);
        if(getMethods.size() > 0) {
            getMethodDeclaration = getMethods.get(0);
            Optional<Comment> optionalComment = getMethodDeclaration.getComment();
            if(optionalComment.isPresent()) {
                Comment comment = optionalComment.get();
                if(comment instanceof BlockComment) {
                    BlockComment blockComment = (BlockComment)optionalComment.get();
                    String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                    // Skip if method already exist with header code
                    if(headerCommands != null && headerCommands.startsWith(DefaultCodeParser.CMD_HEADER_START + idlParser.headerCMD)) {
                        addGet = false;
                    }
                }
            }
        }
        MethodDeclaration setMethodDeclaration = null;
        List<MethodDeclaration> setMethods = classOrInterfaceDeclaration.getMethodsBySignature(attributeName, attributeType);
        if(setMethods.size() > 0) {
            setMethodDeclaration = setMethods.get(0);
            Optional<Comment> optionalComment = setMethodDeclaration.getComment();
            if(optionalComment.isPresent()) {
                Comment comment = optionalComment.get();
                if(comment instanceof BlockComment) {
                    BlockComment blockComment = (BlockComment)optionalComment.get();
                    String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                    // Skip if method already exist with header code
                    if(headerCommands != null && headerCommands.startsWith(DefaultCodeParser.CMD_HEADER_START + idlParser.headerCMD)) {
                        addSet = false;
                    }
                }
            }
        }
        if(addGet && !shouldSkipMethod(containsGetMethod)) {
            if(getMethodDeclaration != null) {
                getMethodDeclaration.remove();
            }
            String getMethodName = "get_" + attributeName;
            getMethodDeclaration = classOrInterfaceDeclaration.addMethod(getMethodName, Modifier.Keyword.PUBLIC);
            getMethodDeclaration.setType(type);
            JParserHelper.addMissingImportType(jParser, unit, type);
            IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, type, getMethodDeclaration);

            if(!idlParser.generateClass) {
                idlParser.onIDLMethodGenerated(jParser, idlClass, null, unit, classOrInterfaceDeclaration, getMethodDeclaration, true);
            }
            else {
                setupAttributeMethod(idlParser, jParser, idlAttribute, false, classOrInterfaceDeclaration, getMethodDeclaration);
            }
        }
        if(addSet && !shouldSkipMethod(containsSetMethod)) {
            if(setMethodDeclaration != null) {
                setMethodDeclaration.remove();
            }
            String setMethodName = "set_" + attributeName;
            setMethodDeclaration = classOrInterfaceDeclaration.addMethod(setMethodName, Modifier.Keyword.PUBLIC);
            setMethodDeclaration.setStatic(idlAttribute.isStatic);
            Parameter parameter = setMethodDeclaration.addAndGetParameter(type, attributeName);
            Type paramType = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, paramType);

            if(!idlParser.generateClass) {
                idlParser.onIDLMethodGenerated(jParser, idlClass, null, unit, classOrInterfaceDeclaration, setMethodDeclaration, true);
            }
            else {
                setupAttributeMethod(idlParser, jParser, idlAttribute, true, classOrInterfaceDeclaration, setMethodDeclaration);
            }
        }
    }

    private static void setupAttributeMethod(IDLDefaultCodeParser idlParser, JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration) {
        boolean isValue = idlAttribute.isValue;

        MethodDeclaration nativeMethod = IDLMethodParser.prepareNativeMethod(idlAttribute.isStatic, isValue, classDeclaration, methodDeclaration);
        if(nativeMethod != null) {
            if(isValue && !isSet) {
                //  When it's a get attribute method we pass a temp c++ object to copy to the returned temp c++ object.
                String pointerTempObject = "copy_addr";
                nativeMethod.addParameter("long", pointerTempObject);
            }
            idlParser.onIDLAttributeGenerated(jParser, idlAttribute, isSet, classDeclaration, methodDeclaration, nativeMethod);
        }
    }

    public static boolean shouldSkipMethod(MethodDeclaration containsMethod) {
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
                            return true;
                        }
                        else {
                            if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                                //If skip is found then remove the method
                                containsMethod.remove();
                            }
                            return true;
                        }
                    }
                }
            }
            if(isNative) {
                // It's a dummy method. Remove it and let IDL generate it again.
                // This is useful to use a base method as an interface and let the generator create the real method.
                containsMethod.remove();
            }
            if(!isNative && !isStatic) {
                // if a simple method exist, keep it and don't let IDL generate the method.
                return true;
            }
        }
        return false;
    }

    private static MethodDeclaration containsSetMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLAttribute idlAttribute) {
        String[] paramTypes = new String[1];
        paramTypes[0] = idlAttribute.type;
        List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethodsBySignature(idlAttribute.name, paramTypes);

        if(methods.size() > 0) {
            return methods.get(0);
        }
        return null;
    }

    private static MethodDeclaration containsGetMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLAttribute idlAttribute) {
        String[] paramTypes = new String[0];
        List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethodsBySignature(idlAttribute.name, paramTypes);

        if(methods.size() > 0) {
            return methods.get(0);
        }
        return null;
    }
}
