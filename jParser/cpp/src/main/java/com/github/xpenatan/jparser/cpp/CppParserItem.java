package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.parsing.JavaMethodParser;
import java.util.ArrayList;

public class CppParserItem {
    public String sourceBaseDir;
    public String inputJavaPath;
    public String destinationJavaPath;
    public final ArrayList<JavaMethodParser.JavaSegment> javaSegments = new ArrayList<>();
}