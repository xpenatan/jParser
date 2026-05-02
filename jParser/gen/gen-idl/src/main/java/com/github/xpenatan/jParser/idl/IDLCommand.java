package com.github.xpenatan.jParser.idl;

import java.util.ArrayList;

public class IDLCommand {

    public static final String CMD_NEW_OBJECT = "NEW_OBJECT";
    public static final String CMD_NEW_PARAM = "NEW_PARAM";
    public static final String CMD_SKIP = "SKIP";
    public static final String CMD_MEM_OWN = "MEM_OWN";
    public static final String CMD_NOT_MEM_OWN = "NOT_MEM_OWN";
    public static final String CMD_SUB_PACKAGE = "SUB_PACKAGE";
    public static final String CMD_RENAME = "RENAME";

    public final ArrayList<String> commands = new ArrayList<>();

    public String command;

    public IDLCommand(String command) {
        this.command = command;
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

    public boolean containsCommand(String command) {
        return containsCommand(commands, command);
    }

    public boolean containsCommandValue(String command, String value) {
        return getCommandValue(commands, command, value) != null;
    }

    public String getCommandValue(String command) {
        return getCommandValue(commands, command, null);
    }

    public static boolean containsCommand(ArrayList<String> commands, String command) {
        for(int i = 0; i < commands.size(); i++) {
            String cmd = commands.get(i);
            if(cmd.startsWith(command)) {
                return true;
            }
        }
        return false;
    }

    public static String getCommandValue(ArrayList<String> commands, String command, String commandValue) {
        for(int i = 0; i < commands.size(); i++) {
            String cmd = commands.get(i);
            if(cmd.startsWith(command)) {
                String value = cmd.split("=")[1].trim();
                if(commandValue != null) {
                    if(commandValue.equals(value)) {
                        return value;
                    }
                }
                else {
                    return value;
                }
            }
        }
        return null;
    }
}
