package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLFile {

    public boolean skip = false;

    public final String idlName;

    public ArrayList<String> lines = new ArrayList<>();

    public final ArrayList<IDLClassOrEnum> classArray = new ArrayList<>();

    public IDLFile(String idlName) {
        this.idlName = idlName;
    }

    public String getIDLStr() {
        String idlStr = "";
        for(String line : lines) {
            if(!line.trim().isEmpty()) {
                idlStr += line + "\n";
            }
        }
        return idlStr;
    }

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

    public IDLEnumClass getEnum(String name) {
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