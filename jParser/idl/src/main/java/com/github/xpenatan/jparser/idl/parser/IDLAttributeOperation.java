package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.idl.IDLAttribute;

public class IDLAttributeOperation {

    public static Op getEnum(boolean isSet, IDLAttribute idlAttribute, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        boolean isStatic = methodDeclaration.isStatic();
        Type nativeReturnType = nativeMethod.getType();
        boolean isValue = idlAttribute.isValue;
        Type returnType = methodDeclaration.getType();
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        if(nativeReturnType.isVoidType() && isValue) {
            if(isSet) {
                if(isStatic) {
                    return Op.SET_OBJECT_VALUE_STATIC;
                }
                else {
                    return Op.SET_OBJECT_VALUE;
                }
            }
            else {
                if(isStatic) {
                    return Op.GET_OBJECT_VALUE_STATIC;
                }
                else {
                    return Op.GET_OBJECT_VALUE;
                }
            }
        }
        else if(returnType.isVoidType()) {
            if(isSet && parameters.size() == 1 && parameters.get(0).getType().isClassOrInterfaceType()) {
                if(isStatic) {
                    return Op.SET_OBJECT_POINTER_STATIC;
                }
                else {
                    return Op.SET_OBJECT_POINTER;
                }
            }
            else {
                if(isStatic) {
                    return Op.SET_PRIMITIVE_STATIC;
                }
                else {
                    return Op.SET_PRIMITIVE;
                }
            }
        }
        else if(returnType.isClassOrInterfaceType()) {
            if(isStatic) {
                return Op.GET_OBJECT_POINTER_STATIC;
            }
            else {
                return Op.GET_OBJECT_POINTER;
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
        SET_OBJECT_VALUE,
        SET_OBJECT_VALUE_STATIC,
        GET_OBJECT_VALUE,
        GET_OBJECT_VALUE_STATIC,

        SET_OBJECT_POINTER,
        SET_OBJECT_POINTER_STATIC,
        GET_OBJECT_POINTER,
        GET_OBJECT_POINTER_STATIC,

        SET_PRIMITIVE,
        SET_PRIMITIVE_STATIC,
        GET_PRIMITIVE,
        GET_PRIMITIVE_STATIC,
    }
}