package com.github.xpenatan.jParser.core.codeparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;
import java.util.ArrayList;

/**
 * @author xpenatan
 */
public interface CodeParser {
    void onParseClassStart(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum);

    void onParseClassEnd(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum);

    void parseCode(CodeParserItem parserItem);

    default void onParseCodeEnd() {
    }

    default void onParseFileStart(JParser jParser, JParserItem parserItem) {
    }

    /**
     * Called only when the file is ready to save.
     */
    default void onParseFileEnd(JParser jParser, JParserItem parserItem) {
    }

    default void onParseStart(JParser jParser) {
    }

    default void onParseEnd(JParser jParser) {
    }

    /**
     * Called when all parsing is complete and is ready to save to file.
     */
    default void onParserComplete(JParser jParser, ArrayList<JParserItem> parserItems) {
    }
}