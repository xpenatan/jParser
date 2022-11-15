package com.github.xpenatan.jparser.core.codeparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.util.RawCodeBlock;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author xpenatan
 */
public abstract class DefaultCodeParser implements CodeParser {

    static final String CMD_HEADER_START = "[-";
    static final String CMD_HEADER_END = "]";
    public static final String CMD_ADD = "-ADD";
    public static final String CMD_REMOVE = "-REMOVE";
    public static final String CMD_REPLACE = "-REPLACE";
    public static final String CMD_NATIVE = "-NATIVE";

    private ArrayList<BlockComment> cache = new ArrayList<>();

    public final String headerCMD;

    public DefaultCodeParser(String headerCMD) {
        this.headerCMD = headerCMD;
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
    }

    @Override
    public void onParseClassEnd(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
    }

    @Override
    public void onParseCodeEnd() {
        for(int i = 0; i < cache.size(); i++) {
            BlockComment otherTopBlockComment = cache.get(i);
            if(CodeParserItem.obtainHeaderCommands(otherTopBlockComment) != null) {
                parserBlock(otherTopBlockComment, otherTopBlockComment);
                otherTopBlockComment.remove();
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
        else if(node instanceof BlockComment) {
            BlockComment standAloneBlockComment = (BlockComment)node;
            cache.add(standAloneBlockComment);
        }
        else {
            //If node does not contains a block comment then just parse all cache nodes.
            onParseCodeEnd();
            return;
        }
        if(blockComment != null) {
            boolean blockParsed = false;
            String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
            if(headerCommands != null) {
                blockComment.remove();
                blockParsed = parserBlock(node, blockComment);
            }
            for(int i = cache.size()-1; i >= 0; i--) {
                BlockComment otherTopBlockComment = cache.get(i);
                if(CodeParserItem.obtainHeaderCommands(otherTopBlockComment) != null) {
                    if(blockParsed) {
                        parserBlock(otherTopBlockComment, otherTopBlockComment);
                    }
                    else {
                        if(parserBlock(node, otherTopBlockComment)) {
                            blockParsed = true;
                        }
                    }
                    otherTopBlockComment.remove();
                }
            }
            cache.clear();
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

    public boolean parseCodeBlock(Node node, String headerCommands, String content) {
        if(headerCommands.contains(CMD_ADD)) {
            setAddReplaceCMD(node, content, false);
            return true;
        }
        else if(headerCommands.contains(CMD_REMOVE)) {
            node.remove();
            return true;
        }
        else if(headerCommands.contains(CMD_REPLACE)) {
            setAddReplaceCMD(node, content, true);
            return true;
        }
        else if(headerCommands.contains(CMD_NATIVE)) {
            if(node instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration)node;
                setJavaBodyNativeCMD(content, methodDeclaration);
                return true;
            }
        }
        return false;
    }

    private void setAddReplaceCMD(Node node, String content, boolean replace) {
        Node parentNode = null;
        Optional<Node> parentNodeOptional = node.getParentNode();
        if(parentNodeOptional.isPresent()) {
            parentNode = parentNodeOptional.get();
        }
        if(parentNode != null) {
            if(parentNode instanceof TypeDeclaration) {
                TypeDeclaration<?> typeDeclaration = (TypeDeclaration<?>)parentNode;
                try {
                    BodyDeclaration<?> newCode = StaticJavaParser.parseBodyDeclaration(content);
                    typeDeclaration.getMembers().add(newCode);
                }
                catch(Throwable t) {
                    String className = typeDeclaration.getNameAsString();
                    System.err.println("Error Class: " + className + "\n" + content);
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

    protected abstract void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration);
}