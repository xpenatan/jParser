package com.github.xpenatan.jParser.cpp;

/**
 * Build-time JNI glue naming policy.
 */
public class JNIClassData {
    public enum SymbolNameMode {
        DEFAULT,
        OBFUSCATED
    }

    /**
     * Naming mode used for generated JNI holder classes/methods.
     */
    public SymbolNameMode symbolNameMode = SymbolNameMode.DEFAULT;

    /**
     * Optional salt used by OBFUSCATED mode.
     */
    public String symbolObfuscationSalt = "";

    /**
     * Root package used when OBFUSCATED mode generates detached holder packages.
     */
    public String obfuscatedRootPackage = "n";
}

