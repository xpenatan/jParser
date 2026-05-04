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

    public FFMClassData() {
    }

    public FFMClassData(boolean defaultCritical) {
        this.defaultCritical = defaultCritical;
    }
}

