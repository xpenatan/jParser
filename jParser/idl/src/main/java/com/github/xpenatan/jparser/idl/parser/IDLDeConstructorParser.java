package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.parser.data.IDLParameterData;
import java.util.ArrayList;
import java.util.List;

public class IDLDeConstructorParser {

    private static final String DELETE_NATIVE = "deleteNative";

    public static void generateDeConstructor(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass) {
        if(!idlClass.classHeader.isNoDelete) {
            List<MethodDeclaration> methodsBySignature = classOrInterfaceDeclaration.getMethodsBySignature(DELETE_NATIVE);
            int size = methodsBySignature.size();

            MethodDeclaration deleteMethod = null;
            if(size == 1) {
                MethodDeclaration tmpMethod = methodsBySignature.get(0);
                if(IDLMethodParser.canGenerateMethod(tmpMethod)) {
                    if(tmpMethod.getParameters().isEmpty()) {
                        deleteMethod = tmpMethod;
                    }
                }
            }
            else if(size == 0) {
                deleteMethod = classOrInterfaceDeclaration.addMethod(DELETE_NATIVE, Modifier.Keyword.PROTECTED);
            }

            if(deleteMethod != null) {
                NodeList<Parameter> parameters = deleteMethod.getParameters();
                Type type = deleteMethod.getType();

                ArrayList<IDLParameterData> parameterArray = new ArrayList<>();
                for(int i = 0; i < parameters.size(); i++) {
                    Parameter parameter = parameters.get(i);
                    IDLParameterData data = new IDLParameterData();
                    data.parameter = parameter;
                    parameterArray.add(data);
                }
                MethodDeclaration nativeMethod = IDLMethodParser.generateNativeMethod(idlParser.idlReader, DELETE_NATIVE, parameterArray, type, false);

                if(!JParserHelper.containsMethod(classOrInterfaceDeclaration, nativeMethod)) {
                    classOrInterfaceDeclaration.getMembers().add(nativeMethod);
                    MethodCallExpr caller = IDLMethodParser.createCaller(nativeMethod);
                    caller.addArgument(IDLDefaultCodeParser.NATIVE_ADDRESS);
                    BlockStmt blockStmt = deleteMethod.getBody().get();
                    blockStmt.addStatement(caller);
                    idlParser.onIDLDeConstructorGenerated(jParser, idlClass, classOrInterfaceDeclaration, nativeMethod);
                }
            }
        }
    }
}
