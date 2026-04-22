package com.github.xpenatan.jParser.ffm;

import com.github.javaparser.ast.body.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks MethodHandle entries per Java class during FFM code generation.
 * After all methods are parsed, this registry is used to inject the FFMHandles inner class
 * with static MethodHandle fields and FunctionDescriptor initialization.
 */
public class FFMMethodHandleRegistry {

    private final Map<String, List<FFMEntry>> classEntries = new HashMap<>();

    /**
     * Register a native method for a given class.
     */
    public void register(String className, String symbolName, String javaMethodName,
                         String handleName, String returnType, List<ParamInfo> parameters) {
        List<FFMEntry> entries = classEntries.computeIfAbsent(className, k -> new ArrayList<>());
        entries.add(new FFMEntry(symbolName, javaMethodName, handleName, returnType, parameters));
    }

    /**
     * Get all entries for a given class.
     */
    public List<FFMEntry> getEntries(String className) {
        return classEntries.getOrDefault(className, new ArrayList<>());
    }

    /**
     * Get all class names that have registered entries.
     */
    public Iterable<String> getClassNames() {
        return classEntries.keySet();
    }

    /**
     * Check if a class has any registered entries.
     */
    public boolean hasEntries(String className) {
        List<FFMEntry> entries = classEntries.get(className);
        return entries != null && !entries.isEmpty();
    }

    /**
     * Clear all entries (for reuse).
     */
    public void clear() {
        classEntries.clear();
    }

    /**
     * Generate the FunctionDescriptor code for a single entry.
     * Example output: "FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)"
     * Example output: "FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG)"
     */
    public static String buildFunctionDescriptor(FFMEntry entry) {
        StringBuilder sb = new StringBuilder();
        boolean isVoid = entry.returnType.equals("void");

        if(isVoid) {
            sb.append("FunctionDescriptor.ofVoid(");
        }
        else {
            String retLayout = FFMTypeMapper.getValueLayout(entry.returnType);
            if(retLayout == null) {
                // Non-primitive return (object pointer → long)
                retLayout = "ValueLayout.JAVA_LONG";
            }
            sb.append("FunctionDescriptor.of(").append(retLayout);
            if(!entry.parameters.isEmpty()) {
                sb.append(", ");
            }
        }

        for(int i = 0; i < entry.parameters.size(); i++) {
            ParamInfo param = entry.parameters.get(i);
            String layout = FFMTypeMapper.getValueLayout(param.javaType);
            if(layout == null) {
                // Non-primitive parameter (object address → long)
                layout = "ValueLayout.JAVA_LONG";
            }
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(layout);
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Represents a single MethodHandle entry to be generated.
     */
    public static class FFMEntry {
        public final String symbolName;
        public final String javaMethodName;
        /** Unique field name for the MethodHandle in FFMHandles (includes overload suffix). */
        public final String handleName;
        public final String returnType;
        public final List<ParamInfo> parameters;

        public FFMEntry(String symbolName, String javaMethodName, String handleName, String returnType, List<ParamInfo> parameters) {
            this.symbolName = symbolName;
            this.javaMethodName = javaMethodName;
            this.handleName = handleName;
            this.returnType = returnType;
            this.parameters = parameters;
        }
    }

    /**
     * Parameter info for building FunctionDescriptor.
     */
    public static class ParamInfo {
        public final String name;
        public final String javaType;

        public ParamInfo(String name, String javaType) {
            this.name = name;
            this.javaType = javaType;
        }

        public static ParamInfo fromParameter(Parameter parameter) {
            String[] typeTokens = parameter.getType().toString().split("\\.");
            String type = typeTokens[typeTokens.length - 1];
            return new ParamInfo(parameter.getNameAsString(), type);
        }
    }
}

