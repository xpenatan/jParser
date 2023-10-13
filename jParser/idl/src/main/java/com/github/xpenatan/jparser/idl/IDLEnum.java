package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLEnum extends IDLClassOrEnum {
    public final IDLFile idlFile;

    public final ArrayList<String> classLines = new ArrayList<>();
    public final ArrayList<String> enums = new ArrayList<>();
    public ArrayList<String> settings = new ArrayList<>();

    public boolean isNameSpace;

    public String typePrefix = "";

    public IDLEnum(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initEnum(ArrayList<String> lines) {
        classLines.addAll(lines);
        setupInterfaceName();
        setupEnumValues();
        setupSettings();
    }

    private void setupInterfaceName() {
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

    private void setupEnumValues() {
        for(int i = 1; i < classLines.size()-1; i++) {
            String enumLine = classLines.get(i);
            String[] split = enumLine.split(",");
            for(String s : split) {
                enumLine = s.replace("\"", "").trim();
                if(typePrefix.isEmpty() && enumLine.contains("::")) {
                    typePrefix = enumLine.split("::")[0] + "::";
                }
                enums.add(enumLine);
            }
        }
    }

    private void setupSettings() {
        for(String option : settings) {
            if(option.equals("[-NAMESPACE]")) {
                isNameSpace = true;
            }
        }
    }

    public String getName() {
        return name;
    }
}