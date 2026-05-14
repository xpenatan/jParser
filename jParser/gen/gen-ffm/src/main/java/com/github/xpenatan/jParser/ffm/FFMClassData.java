package com.github.xpenatan.jParser.ffm;

/**
 * Build-time policy for generated FFM downcall critical mode.
 */
public class FFMClassData {
    public enum SymbolNameMode {
        DEFAULT,
        OBFUSCATED
    }

    /**
     * Default critical mode for generated methods.
     */
    public boolean defaultCritical = true;

    /**
     * Optional per-method override.
     */
    public FFMCriticalMethodListener methodListener;

    /**
     * Symbol naming mode for generated FFM glue exports.
     */
    public SymbolNameMode symbolNameMode = SymbolNameMode.DEFAULT;

    /**
     * Optional salt used by OBFUSCATED symbol mode.
     */
    public String symbolObfuscationSalt = "";

    /**
     * When enabled, generated String-return bridges use a bounded reinterpret length.
     */
    public boolean boundedStringReturn = false;

    /**
     * Max bytes used when bounded String-return decode is enabled.
     */
    public long boundedStringReturnMaxBytes = 4096L;

    public FFMClassData() {
    }
}

