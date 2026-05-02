package com.github.xpenatan.jParser.loader;

public interface JParserLibraryLoaderListener {
    void onLoad(boolean isSuccess, Throwable t);
}