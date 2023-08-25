package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLEnum extends IDLClassOrEnum {
    public final IDLFile idlFile;

    public final ArrayList<String> classLines = new ArrayList<>();
    public final ArrayList<String> enums = new ArrayList<>();

    public IDLEnum(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initEnum(ArrayList<String> lines) {
        classLines.addAll(lines);
        setInterfaceName();
        setEnumValues();
    }

    private void setInterfaceName() {
        String nameLine = null;
        for(int i = 0; i < classLines.size(); i++) {
            String line = classLines.get(i);
            if(line.contains("enum ")) {
                nameLine = line;
                break;
            }
        }
        if(nameLine != null) {
            name = nameLine.split(" ")[1];
        }
    }

    private void setEnumValues() {
        for(int i = 1; i < classLines.size()-1; i++) {
            String enumLine = classLines.get(i);
            enumLine = enumLine.replace(",", "").replace("\"", "");
            enums.add(enumLine);
        }
    }

    public String getName() {
        return name;
    }
}