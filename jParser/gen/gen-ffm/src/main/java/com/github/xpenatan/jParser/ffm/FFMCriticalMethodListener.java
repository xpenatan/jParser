package com.github.xpenatan.jParser.ffm;

/**
 * Listener used to override critical mode generation for each native method.
 * Returning null keeps the class default policy.
 */
public interface FFMCriticalMethodListener {
    FFMCriticalMode onCriticalMode(FFMCriticalMethodData methodData);
}

