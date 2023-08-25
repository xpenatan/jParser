package com.github.xpenatan.jparser.idl;

/**
 * @author xpenatan
 */
public class IDLClassOrEnum {

    public String name;

    public boolean isEnum() {
        return this instanceof IDLEnum;
    }

    public boolean isClass() {
        return this instanceof IDLClass;
    }

    public IDLClass asClass() {
        return (IDLClass)this;
    }

    public IDLEnum asEnum() {
        return (IDLEnum)this;
    }
}