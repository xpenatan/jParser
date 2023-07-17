package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLFile {
    public final String subPackage;
    public final ArrayList<IDLClass> classArray = new ArrayList<>();

    public IDLFile(String subPackage) {
        this.subPackage = subPackage;
    }

    public IDLClass getClass(String name) {
        int size = classArray.size();
        for(int i = 0; i < size; i++) {
            IDLClass idlClass = classArray.get(i);
            if(idlClass.name.equals(name)) {
                return idlClass;
            }
        }
        return null;
    }
}