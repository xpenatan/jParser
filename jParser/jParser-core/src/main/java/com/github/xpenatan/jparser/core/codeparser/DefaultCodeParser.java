package com.github.xpenatan.jparser.core.codeparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.RawCodeBlock;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author xpenatan
 */
public abstract class DefaultCodeParser implements CodeParser {
    public static final String IDL_BASE_CLASS = "IDLBase";

    public static final String CMD_HEADER_START = "[-";
    public static final String CMD_HEADER_END = "]";
    public static final String CMD_ADD = "-ADD";
    public static final String CMD_ADD_RAW = "-ADD_RAW";
    public static final String CMD_REMOVE = "-REMOVE";
    public static final String CMD_REPLACE = "-REPLACE";
    public static final String CMD_REPLACE_BLOCK = "-REPLACE_BLOCK";
    public static final String CMD_NATIVE = "-NATIVE";

    private ArrayList<BlockComment> cache = new ArrayList<>();

    public final String headerCMD;

    public DefaultCodeParser(String headerCMD) {
        this.headerCMD = headerCMD;
    }

    protected boolean shouldRemoveCommentBlock(String headerCommands) {
        if(!headerCommands.startsWith(CMD_HEADER_START + headerCMD)) {
            return true;
        }
        return false;
    }

    @Override
    public void onParseFileStart(JParser jParser, JParserItem parserItem) {
        CompilationUnit unit = parserItem.unit;
        for(Comment allComment : unit.getAllComments()) {
            if(allComment.isBlockComment()) {
                BlockComment blockComment = allComment.asBlockComment();
                String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                if(headerCommands != null) {
                    if(shouldRemoveCommentBlock(headerCommands)) {
                        blockComment.remove();
                    }
                }
            }
        }
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum) {
    }

    @Override
    public void onParseClassEnd(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum) {
    }

    @Override
    public void onParseCodeEnd() {
        for(int i = 0; i < cache.size(); i++) {
            BlockComment otherTopBlockComment = cache.get(i);
            if(CodeParserItem.obtainHeaderCommands(otherTopBlockComment) != null) {
                parserBlock(otherTopBlockComment, otherTopBlockComment);
//                otherTopBlockComment.remove();
            }
        }
        cache.clear();
    }

    @Override
    public void parseCode(CodeParserItem parserItem) {
        BlockComment blockComment = null;
        Node node = parserItem.node;
        Optional<Comment> commentOptional = node.getComment();
        if(commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            if(comment.isBlockComment()) {
                blockComment = comment.asBlockComment();
            }
        }
        else if(node instanceof RawCodeBlock) {
            RawCodeBlock rawCodeBlock = (RawCodeBlock)node;
            String content = rawCodeBlock.getContent();
            content = content.replace("/*", "");
            content = content.replace("*/", "");
            //Block comment already add tags
            BlockComment comment = new BlockComment(content);
            cache.add(comment);
            return;
        }
        else if(node instanceof BlockComment) {
            BlockComment standAloneBlockComment = (BlockComment)node;
            cache.add(standAloneBlockComment);
            return;
        }

        if(node instanceof MethodDeclaration) {
            boolean blockParsed = false;
            if(blockComment != null) {
                String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                if(headerCommands != null) {
//                blockComment.remove();
                    blockParsed = parserBlock(node, blockComment);
                }
            }
            while(cache.size() > 0) {
                //get the last cache item to check if it matches. If it has been parsed then change the order
                BlockComment otherTopBlockComment = null;
                if(blockParsed) {
                    otherTopBlockComment = cache.remove(0);
                }
                else {
                    otherTopBlockComment = cache.remove(cache.size()-1);
                }
                if(CodeParserItem.obtainHeaderCommands(otherTopBlockComment) != null) {
                    if(blockParsed) {
                        parserBlock(otherTopBlockComment, otherTopBlockComment);
                    }
                    else {
                        if(parserBlock(node, otherTopBlockComment)) {
                            blockParsed = true;
                        }
                    }
                }
            }
        }
        else {
            if(blockComment != null) {
                String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                if(headerCommands != null) {
                    parserBlock(node, blockComment);
                }
            }
            //If node does not contains a block comment then just parse all cache nodes.
            onParseCodeEnd();
        }
    }

    private boolean parserBlock(Node node, BlockComment blockComment) {
        String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
        if(headerCommands != null) {
            if(headerCommands.startsWith(CMD_HEADER_START + headerCMD) && headerCommands.endsWith(CMD_HEADER_END)) {
                String content = CodeParserItem.obtainContent(headerCommands, blockComment);
                parseCodeBlock(node, headerCommands, content);
                return true;
            }
        }
        return false;
    }

    protected boolean parseCodeBlock(Node node, String headerCommands, String content) {
        if(headerCommands.contains(CMD_ADD_RAW)) {
            setAddReplaceCMD(node, content, false, true, false);
            return true;
        }
        else if(headerCommands.contains(CMD_ADD)) {
            setAddReplaceCMD(node, content, false, false, false);
            return true;
        }
        else if(headerCommands.contains(CMD_REMOVE)) {
            node.remove();
            return true;
        }
        else if(headerCommands.contains(CMD_REPLACE_BLOCK)) {
            if(node instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration)node;
                BlockStmt blockStmt = StaticJavaParser.parseBlock(content);
                methodDeclaration.setBody(blockStmt);
                return true;
            }
            return false;
        }
        else if(headerCommands.contains(CMD_REPLACE)) {
            setAddReplaceCMD(node, content, true, false, false);
            return true;
        }
        else if(headerCommands.contains(CMD_NATIVE)) {
            if(node instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration)node;
                if(methodDeclaration.isNative()) {
                    setJavaBodyNativeCMD(content, methodDeclaration);
                    return true;
                }
            }
        }
        return false;
    }

    private void setAddReplaceCMD(Node node, String content, boolean replace, boolean rawAdd, boolean replaceBlock) {
        Node parentNode = null;
        Optional<Node> parentNodeOptional = node.getParentNode();
        if(parentNodeOptional.isPresent()) {
            parentNode = parentNodeOptional.get();
        }
        if(parentNode != null) {
            if(parentNode instanceof TypeDeclaration) {
                TypeDeclaration<?> typeDeclaration = (TypeDeclaration<?>)parentNode;
                try {
                    if(rawAdd) {
                        RawCodeBlock newblockComment = new RawCodeBlock();
                        newblockComment.setContent(content);
                        Optional<TokenRange> tokenRange = node.getTokenRange();
                        TokenRange javaTokens = tokenRange.get();
                        newblockComment.setTokenRange(javaTokens);
                        typeDeclaration.getMembers().add(newblockComment);
                    }
                    else {
                        BodyDeclaration<?> newCode = StaticJavaParser.parseBodyDeclaration(content);
                        typeDeclaration.getMembers().add(newCode);
                    }
                }
                catch(Throwable t) {
                    String className = typeDeclaration.getNameAsString();
                    System.err.println("Error Class: " + className + "\nError conent: " + content);
                    throw t;
                }
            }
            else if(parentNode instanceof CompilationUnit) {
                CompilationUnit unit = (CompilationUnit)parentNode;
                RawCodeBlock newblockComment = new RawCodeBlock();
                newblockComment.setContent(content);
                Optional<TokenRange> tokenRange = node.getTokenRange();
                TokenRange javaTokens = tokenRange.get();
                newblockComment.setTokenRange(javaTokens);
                unit.addType(newblockComment);
            }
        }
        if(replace) {
            node.remove();
        }
    }

    protected abstract void setJavaBodyNativeCMD(String content, MethodDeclaration nativeMethodDeclaration);
}