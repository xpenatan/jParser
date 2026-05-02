package com.github.xpenatan.jParser.idl;

public class IDLLine {
    public final String line;
    public final String comment;
    public final IDLCommand idlCommand;

    private IDLLine(String code, String comment, IDLCommand idlCommand) {
        this.line = code;
        this.comment = comment;
        this.idlCommand = idlCommand;
    }

    public IDLLine(String code, String comment, String command) {
        this.line = code;
        this.comment = comment;
        this.idlCommand = new IDLCommand(command);
    }

    public IDLLine copy() {
        return new IDLLine(line, comment, idlCommand);
    }
}
