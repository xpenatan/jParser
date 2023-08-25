package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLFile {
    public final ArrayList<IDLClassOrEnum> classArray = new ArrayList<>();

    public IDLClass getClass(String name) {
        int size = classArray.size();
        for(int i = 0; i < size; i++) {
            IDLClassOrEnum idlClass = classArray.get(i);
            if(idlClass.name.equals(name)) {
                if(idlClass.isClass()) {
                    return idlClass.asClass();
                }
            }
        }
        return null;
    }

    public IDLEnum getEnum(String name) {
        int size = classArray.size();
        for(int i = 0; i < size; i++) {
            IDLClassOrEnum idlClass = classArray.get(i);
            if(idlClass.name.equals(name)) {
                if(idlClass.isEnum()) {
                    return idlClass.asEnum();
                }
            }
        }
        return null;
    }
}