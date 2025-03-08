package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

public class IDLLine {
    public static final String CMD_NEW_OBJECT = "NEW_OBJECT";
    public static final String CMD_MEM_OWN = "MEM_OWN";
    public static final String CMD_NOT_MEM_OWN = "NOT_MEM_OWN";

    public final String line;
    public final String comment;
    public final ArrayList<String> commands = new ArrayList<>();

    private IDLLine(String code, String comment, ArrayList<String> commands) {
        this.line = code;
        this.comment = comment;
        this.commands.addAll(commands);
    }

    public IDLLine(String code, String comment, String command) {
        this.line = code;
        this.comment = comment;

        if(command != null) {
            command = command.replace("[-", "").replace("]", "").trim();
            String[] commands = command.split(",");
            for(int i = 0; i < commands.length; i++) {
                String cmd = commands[i].trim();
                if(!cmd.isEmpty()) {
                    this.commands.add(cmd);
                }
            }
        }
    }

    public IDLLine copy() {
        return new IDLLine(line, comment, commands);
    }

    public boolean containsCommand(String command) {
        for(int i = 0; i < commands.size(); i++) {
            String cmd = commands.get(i);
            if(cmd.equals(command)) {
                return true;
            }
        }

        return false;
    }
}
