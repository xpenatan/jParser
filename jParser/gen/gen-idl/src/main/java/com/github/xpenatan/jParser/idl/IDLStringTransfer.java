package com.github.xpenatan.jParser.idl;

import java.util.List;

/** Shared metadata and native glue for explicitly transferred String inputs. */
public final class IDLStringTransfer {
    private IDLStringTransfer() {
    }

    static void configure(IDLLine line, List<IDLParameter> parameters) {
        for(String command : line.idlCommand.commands) {
            if(!command.startsWith(IDLCommand.CMD_OWNED_STRING)) continue;
            String[] parts = command.split("=", -1);
            if(parts.length != 2 || !parts[0].trim().equals(IDLCommand.CMD_OWNED_STRING)
                    || parts[1].trim().isEmpty()) {
                throw invalid(line, "expected OWNED_STRING=parameterName");
            }
            String name = parts[1].trim();
            IDLParameter target = null;
            for(IDLParameter parameter : parameters) {
                if(parameter.name.equals(name)) target = parameter;
            }
            if(target == null) throw invalid(line, "unknown parameter " + name);
            if(!"DOMString".equals(target.idlType) || target.isArray || target.isRef || target.isValue) {
                throw invalid(line, name + " must be a scalar DOMString input");
            }
            target.isStringTransfer = true;
        }
    }

    public static boolean hasTransfers(List<IDLParameter> parameters) {
        for(IDLParameter parameter : parameters) {
            if(parameter.isStringTransfer) return true;
        }
        return false;
    }

    public static String declarations(List<IDLParameter> parameters) {
        return declarations(parameters, null);
    }

    public static String declarations(List<IDLParameter> parameters, List<String> nativeNames) {
        StringBuilder code = new StringBuilder();
        for(int i = 0; i < parameters.size(); i++) {
            if(parameters.get(i).isStringTransfer) {
                String input = nativeNames == null ? parameters.get(i).name : nativeNames.get(i);
                code.append("Native::StringTransfer ").append(ownerName(parameters, i))
                        .append("(").append(input).append(");\n");
            }
        }
        return code.toString();
    }

    public static String argument(List<IDLParameter> parameters, int index, String input) {
        return parameters.get(index).isStringTransfer ? ownerName(parameters, index) + ".release()" : input;
    }

    private static String ownerName(List<IDLParameter> parameters, int index) {
        String name = "jparser_owned_string_" + index;
        boolean collision;
        do {
            collision = false;
            for(IDLParameter parameter : parameters) {
                if(parameter.name.equals(name)) {
                    name += "_";
                    collision = true;
                    break;
                }
            }
        } while(collision);
        return name;
    }

    private static IllegalArgumentException invalid(IDLLine line, String detail) {
        return new IllegalArgumentException("Invalid OWNED_STRING: " + detail + " in " + line.line);
    }
}
