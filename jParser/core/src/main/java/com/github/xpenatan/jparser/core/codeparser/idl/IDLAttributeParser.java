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
        if(addGet) {
            if(getMethodDeclaration != null) {
                getMethodDeclaration.remove();
            }
            getMethodDeclaration = classOrInterfaceDeclaration.addMethod(attributeName, Modifier.Keyword.PUBLIC);
            getMethodDeclaration.setType(type);
            JParserHelper.addMissingImportType(jParser, unit, type);
            IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, type, getMethodDeclaration);

            if(!idlParser.generateClass) {
                idlParser.onIDLMethodGenerated(jParser, idlClass, null, unit, classOrInterfaceDeclaration, getMethodDeclaration, true);
            }
        }
        if(addSet) {
            if(setMethodDeclaration != null) {
                setMethodDeclaration.remove();
            }
            setMethodDeclaration = classOrInterfaceDeclaration.addMethod(attributeName, Modifier.Keyword.PUBLIC);
            setMethodDeclaration.setStatic(idlAttribute.isStatic);
            Parameter parameter = setMethodDeclaration.addAndGetParameter(type, attributeName);
            Type paramType = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, paramType);

            if(!idlParser.generateClass) {
                idlParser.onIDLMethodGenerated(jParser, idlClass, null, unit, classOrInterfaceDeclaration, setMethodDeclaration, true);
            }
        }
    }
}
