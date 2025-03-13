package com.github.xpenatan.jparser.loader;

public interface JParserLibraryLoaderListener {
    void onLoad(boolean isSuccess, Exception e);
}