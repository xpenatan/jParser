package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.idl.IDLMethod;

public class IDLMethodOperation {

    public static Op getEnum(IDLMethod idlMethod, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        boolean isStatic = methodDeclaration.isStatic();
        boolean isReturnRef = idlMethod.isReturnRef;
        boolean isReturnValue = idlMethod.isReturnValue;
        Type returnType = methodDeclaration.getType();
        boolean isReturnTypeEnum = idlMethod.returnClassType != null && idlMethod.returnClassType.isEnum();
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        if(returnType.isVoidType()) {
            if(isStatic) {
                return Op.CALL_VOID_STATIC;
            }
            else {
                return Op.CALL_VOID;
            }
        }
        else if(!isReturnTypeEnum && returnType.isClassOrInterfaceType()) {
            if(isReturnRef) {
                if(isStatic) {
                    return Op.GET_OBJ_REF_POINTER_STATIC;
                }
                else {
                    return Op.GET_OBJ_REF_POINTER;
                }
            }
            else if(isReturnValue) {
                if(isStatic) {
                    return Op.GET_OBJ_VALUE_STATIC;
                }
                else {
                    return Op.GET_OBJ_VALUE;
                }
            }
            else {
                if(isStatic) {
                    return Op.GET_OBJ_POINTER_STATIC;
                }
                else {
                    return Op.GET_OBJ_POINTER;
                }
            }
        }
        else {
            if(isStatic) {
                return Op.GET_PRIMITIVE_STATIC;
            }
            else {
                return Op.GET_PRIMITIVE;
            }
        }
    }

    public enum Op {
        CALL_VOID_STATIC,
        CALL_VOID,
        GET_OBJ_REF_POINTER_STATIC,
        GET_OBJ_REF_POINTER,
        GET_OBJ_VALUE_STATIC,
        GET_OBJ_VALUE,
        GET_OBJ_POINTER_STATIC,
        GET_OBJ_POINTER,
        GET_PRIMITIVE_STATIC,
        GET_PRIMITIVE,
    }
}