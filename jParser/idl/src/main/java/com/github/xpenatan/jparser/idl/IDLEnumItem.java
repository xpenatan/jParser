package com.github.xpenatan.jparser.idl;

public class IDLEnumItem {
    public IDLEnum idlEnum;
    public String name;
    public IDLLine idlLine;

    public IDLEnumItem(IDLEnum idlEnum, String name, IDLLine idlLine) {
        this.idlEnum = idlEnum;
        this.name = name;
        this.idlLine = idlLine;
    }

    public String getRenamedName() {
        if(idlLine.idlCommand.containsCommand(IDLCommand.CMD_RENAME)) {
            String commandValue = idlLine.idlCommand.getCommandValue(IDLCommand.CMD_RENAME);
            return commandValue;
        }
        return null;
    }
}