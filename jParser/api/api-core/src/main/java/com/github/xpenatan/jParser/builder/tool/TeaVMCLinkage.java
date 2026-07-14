package com.github.xpenatan.jParser.builder.tool;

/** Native-library linkage modes supported by jParser's TeaVM C runtime. */
public enum TeaVMCLinkage {
    /** Links all native code into the application executable. */
    STATIC,

    /** Links a shared library that the operating system loads at application startup. */
    SHARED_LINKED,

    /** Loads and resolves the native library explicitly at runtime. */
    RUNTIME_LOADED
}
