package com.github.xpenatan.jParser.idl.parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jParser.idl.IDLAttribute;

public class IDLAttributeOperation {

    public static Op getEnum(boolean isSet, IDLAttribute idlAttribute, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        boolean isStatic = methodDeclaration.isStatic();
        boolean isValue = idlAttribute.isValue;
        boolean isArray = idlAttribute.isArray;
        boolean isReturnAny = idlAttribute.isAny;
        Type returnType = methodDeclaration.getType();
        boolean isReturnTypeEnum = idlAttribute.idlClassOrEnum != null && idlAttribute.idlClassOrEnum.isEnum();
        boolean isReturnTypeInterface = returnType.isClassOrInterfaceType() && !isReturnAny;
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        if(returnType.isVoidType()) {
            if(parameters.size() == 1) {
                //Set
                if(!isReturnTypeEnum && parameters.get(0).getType().isClassOrInterfaceType()) {
                    if(isValue) {
                        if(isStatic) {
                            return Op.SET_OBJECT_VALUE_STATIC;
                        }
                        else {
                            return Op.SET_OBJECT_VALUE;
                        }
                    }
                    else {
                        // is pointer
                        if(isStatic) {
                            return Op.SET_OBJECT_POINTER_STATIC;
                        }
                        else {
                            return Op.SET_OBJECT_POINTER;
                        }
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
            else if(isArray) {
                if(parameters.get(1).getType().isClassOrInterfaceType()) {
                    if(isValue) {
                        if(isStatic) {
                            return Op.SET_ARRAY_OBJECT_VALUE_STATIC;
                        }
                        else {
                            return Op.SET_ARRAY_OBJECT_VALUE;
                        }
                    }
                    else {
                        // is pointer
                        if(isStatic) {
                            return Op.SET_ARRAY_OBJECT_POINTER_STATIC;
                        }
                        else {
                            return Op.SET_ARRAY_OBJECT_POINTER;
                        }
                    }
                }
                else {
                    if(isStatic) {
                        return Op.SET_ARRAY_PRIMITIVE_STATIC;
                    }
                    else {
                        return Op.SET_ARRAY_PRIMITIVE;
                    }
                }
            }
        }
        else if(!isReturnTypeEnum && isReturnTypeInterface) {
            if(isArray) {
                if(isValue) {
                    if(isStatic) {
                        return Op.GET_ARRAY_OBJECT_VALUE_STATIC;
                    }
                    else {
                        return Op.GET_ARRAY_OBJECT_VALUE;
                    }
                }
                else {
                    if(isStatic) {
                        return Op.GET_ARRAY_OBJECT_POINTER_STATIC;
                    }
                    else {
                        return Op.GET_ARRAY_OBJECT_POINTER;
                    }
                }
            }
            else {
                if(isValue) {
                    if(isStatic) {
                        return Op.GET_OBJECT_VALUE_STATIC;
                    }
                    else {
                        return Op.GET_OBJECT_VALUE;
                    }
                }
                else {
                    if(isStatic) {
                        return Op.GET_OBJECT_POINTER_STATIC;
                    }
                    else {
                        return Op.GET_OBJECT_POINTER;
                    }
                }
            }
        }
        else {
            if(isArray) {
                if(isStatic) {
                    return Op.GET_ARRAY_PRIMITIVE_STATIC;
                }
                else {
                    return Op.GET_ARRAY_PRIMITIVE;
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
        return null;
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

        // Array

        SET_ARRAY_OBJECT_VALUE,
        SET_ARRAY_OBJECT_VALUE_STATIC,
        GET_ARRAY_OBJECT_VALUE,
        GET_ARRAY_OBJECT_VALUE_STATIC,

        SET_ARRAY_OBJECT_POINTER,
        SET_ARRAY_OBJECT_POINTER_STATIC,
        GET_ARRAY_OBJECT_POINTER,
        GET_ARRAY_OBJECT_POINTER_STATIC,

        SET_ARRAY_PRIMITIVE,
        SET_ARRAY_PRIMITIVE_STATIC,
        GET_ARRAY_PRIMITIVE,
        GET_ARRAY_PRIMITIVE_STATIC,
    }
}