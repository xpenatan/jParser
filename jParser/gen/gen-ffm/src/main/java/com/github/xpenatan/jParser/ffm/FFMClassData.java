package com.github.xpenatan.jParser.ffm;

/**
 * Build-time policy for generated FFM downcall critical mode.
 */
public class FFMClassData {
    /**
     * Default critical mode for generated methods.
     */
    public boolean defaultCritical = true;

    /**
     * Optional per-method override.
     */
    public FFMCriticalMethodListener methodListener;

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

    public FFMClassData(boolean defaultCritical) {
        this.defaultCritical = defaultCritical;
    }
}

