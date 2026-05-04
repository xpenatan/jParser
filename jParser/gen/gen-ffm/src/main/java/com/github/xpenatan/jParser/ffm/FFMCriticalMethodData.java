package com.github.xpenatan.jParser.ffm;

import java.util.List;

/**
 * Immutable method metadata passed to {@link FFMCriticalMethodListener}.
 */
public class FFMCriticalMethodData {
    public final String className;
    public final String symbolName;
    public final String javaMethodName;
    public final String handleName;
    public final String returnType;
    public final List<FFMMethodHandleRegistry.ParamInfo> parameters;
    public final boolean callbackRelatedByIDL;
    public final boolean criticalEligibleByType;

    public FFMCriticalMethodData(String className,
                                 String symbolName,
                                 String javaMethodName,
                                 String handleName,
                                 String returnType,
                                 List<FFMMethodHandleRegistry.ParamInfo> parameters,
                                 boolean callbackRelatedByIDL,
                                 boolean criticalEligibleByType) {
        this.className = className;
        this.symbolName = symbolName;
        this.javaMethodName = javaMethodName;
        this.handleName = handleName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.callbackRelatedByIDL = callbackRelatedByIDL;
        this.criticalEligibleByType = criticalEligibleByType;
    }
}

