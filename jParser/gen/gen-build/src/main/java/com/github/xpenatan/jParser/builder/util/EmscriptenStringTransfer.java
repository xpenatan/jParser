package com.github.xpenatan.jParser.builder.util;

import com.github.xpenatan.jParser.idl.IDLClass;
import com.github.xpenatan.jParser.idl.IDLClassOrEnum;
import com.github.xpenatan.jParser.idl.IDLConstructor;
import com.github.xpenatan.jParser.idl.IDLMethod;
import com.github.xpenatan.jParser.idl.IDLParameter;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.IDLStringTransfer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Applies jParser's input ownership command to the stock WebIDL binder output. */
public final class EmscriptenStringTransfer {
    private EmscriptenStringTransfer() {
    }

    public static String apply(IDLReader reader, String glue) {
        String original = glue;
        Set<String> processed = new HashSet<>();
        for(IDLClassOrEnum entry : reader.getAllClasses()) {
            if(!entry.isClass()) continue;
            IDLClass type = entry.asClass();
            for(IDLConstructor constructor : type.constructors) {
                glue = apply(glue, type.name, type.name, constructor.parameters, processed);
            }
            Set<IDLClass> ancestors = new HashSet<>();
            for(IDLClass owner = type; owner != null && ancestors.add(owner); owner = reader.getClass(owner.extendClass)) {
                for(IDLMethod method : owner.methods) {
                    glue = apply(glue, type.name, method.name, method.parameters, processed);
                }
            }
        }
        return glue.equals(original) ? glue : "#include \"RuntimeHelper.h\"\n" + glue;
    }

    private static String apply(String glue, String type, String method, List<IDLParameter> parameters,
                                Set<String> processed) {
        String symbol = "emscripten_bind_" + type + "_" + method + "_" + parameters.size();
        if(!processed.add(symbol) || !IDLStringTransfer.hasTransfers(parameters)) return glue;
        Pattern function = Pattern.compile("\\b" + Pattern.quote(symbol) + "\\(([^)\\r\\n]*)\\)\\s*\\{([^{}]*)\\}");
        Matcher match = function.matcher(glue);
        if(!match.find()) throw invalid(symbol);
        String[] signature = match.group(1).split(",");
        int offset = signature.length - parameters.size();
        if(offset < 0 || offset > 1) throw invalid(symbol);
        List<String> names = new ArrayList<>();
        for(int i = 0; i < parameters.size(); i++) {
            Matcher name = Pattern.compile("([A-Za-z_][A-Za-z_0-9]*)\\s*$").matcher(signature[i + offset]);
            if(!name.find()) throw invalid(symbol);
            names.add(name.group(1));
        }
        String body = match.group(2);
        for(int i = 0; i < parameters.size(); i++) {
            if(!parameters.get(i).isStringTransfer) continue;
            Matcher argument = Pattern.compile("(?<=[(,])\\s*(" + Pattern.quote(names.get(i))
                    + ")\\s*(?=[,)])").matcher(body);
            if(!argument.find()) throw invalid(symbol);
            int start = argument.start(1);
            int end = argument.end(1);
            if(argument.find()) throw invalid(symbol);
            body = body.substring(0, start) + IDLStringTransfer.argument(parameters, i, names.get(i)) + body.substring(end);
        }
        body = "\n" + IDLStringTransfer.declarations(parameters, names) + body;
        int start = match.start(2);
        int end = match.end(2);
        if(match.find()) throw invalid(symbol);
        return glue.substring(0, start) + body + glue.substring(end);
    }

    private static IllegalArgumentException invalid(String symbol) {
        return new IllegalArgumentException("Cannot apply OWNED_STRING to WebIDL glue: " + symbol);
    }
}
