package com.github.xpenatan.jParser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLEnumClass extends IDLClassOrEnum {
    public final IDLFile idlFile;

    public final ArrayList<IDLEnumItem> enums = new ArrayList<>();
    public ArrayList<String> settings = new ArrayList<>();

    public boolean isNameSpace;

    public String typePrefix = "";

    public IDLEnumClass(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initEnum(ArrayList<String> lines) {
        setupLines(lines);
        setupInterfaceName();
        setupInterfacePackage();
        setupEnumValues();
        setupSettings();

        IDLLine idlLine = searchLine("enum ", true);
        if(idlLine != null) {
            idlSkip = idlLine.idlCommand.containsCommand(IDLCommand.CMD_SKIP);
        }
    }

    private void setupInterfaceName() {
        IDLLine idlLine = searchLine("enum ", true);
        if(idlLine != null) {
            name = idlLine.line.split(" ")[1].trim();
        }
    }

    private void setupInterfacePackage() {
        IDLLine idlLine = searchLine("enum ", true);
        if(idlLine != null && idlLine.idlCommand.containsCommand(IDLCommand.CMD_SUB_PACKAGE)) {
            subPackage = idlLine.idlCommand.getCommandValue(IDLCommand.CMD_SUB_PACKAGE);
        }
    }

    private void setupEnumValues() {
        for(int i = 1; i < classLines.size()-1; i++) {
            IDLLine idlLine = classLines.get(i);
            String enumLine = idlLine.line;
            String[] split = enumLine.split(",");
            for(String s : split) {
                enumLine = s.replace("\"", "").trim();
                if(typePrefix.isEmpty() && enumLine.contains("::")) {
                    typePrefix = enumLine.split("::")[0];
                }
                IDLEnumItem item = new IDLEnumItem(this, enumLine, idlLine);
                enums.add(item);
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