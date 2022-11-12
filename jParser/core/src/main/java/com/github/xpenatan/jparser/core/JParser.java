package com.github.xpenatan.jparser.core;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.utils.PositionUtils;
import com.github.xpenatan.jparser.core.codeparser.CodeParser;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import com.github.xpenatan.jparser.core.util.CustomPrettyPrinter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * JParser is a simple solution to change the original java source with a custom code.
 * It searches for code blocks with a specific tag to change it.
 * <p>
 * How it works:
 * The DefaultCodeParser searches for a header tag staring with '[-HEADER' and ending with ']'.
 * Then it will call the code parser listener, so it can modify the original source code.
 * <p>
 * DefaultCodeParser is an abstract class that does most of the work to -ADD, -REPLACE or -REMOVE the code block with your own custom code.
 * The -NATIVE tag needs to be implemented, and it only works with native methods.
 * DefaultCodeParser will remove the code block automatically if the HEADER tag does not match.
 *
 * @author xpenatan
 */
public class JParser {

    private JParser() {
    }

    public ArrayList<JParserItem> unitArray = new ArrayList<>();

    public JParserItem getParserUnitItem(String className) {
        for(int i = 0; i < unitArray.size(); i++) {
            JParserItem jParserUnitItem = unitArray.get(i);
            if(jParserUnitItem.className.equals(className)) {
                return jParserUnitItem;
            }
        }
        return null;
    }

    static String gen = "-------------------------------------------------------\n"
            + " * This file was generated by JParser\n"
            + " *\n * Do not make changes to this file\n"
            + " *-------------------------------------------------------";

    public static void generate(CodeParser wrapper, String sourceDir, String genDir, String[] excludes) throws Exception {
        CustomFileDescriptor fileSourceDir = new CustomFileDescriptor(sourceDir);
        CustomFileDescriptor fileGenDir = new CustomFileDescriptor(genDir);

        // check if source directory exists
        if(!fileSourceDir.exists()) {
            throw new Exception("Java source directory '" + sourceDir + "' does not exist");
        }

        if(!fileGenDir.exists()) {
            if(!fileGenDir.mkdirs()) {
                throw new Exception("Couldn't create directory '" + genDir + "'");
            }
        }
        else {
            fileGenDir.deleteDirectory();
            if(!fileGenDir.mkdirs()) {
                throw new Exception("Couldn't create directory '" + genDir + "'");
            }
        }
        System.out.println("***** GENERATING CODE *****");
        JParser jParser = new JParser();
        processDirectory(jParser, wrapper, fileSourceDir, fileGenDir, excludes, fileSourceDir);
        for(int i = 0; i < jParser.unitArray.size(); i++) {
            JParserItem parserItem = jParser.unitArray.get(i);
            String inputPath = parserItem.inputPath;
            String destinationPath = parserItem.destinationPath;

            System.out.println(i + " Parsing: " + inputPath);

            String codeParsed = parseJava(jParser, wrapper, parserItem);
            if(codeParsed != null) {
                generateFile(destinationPath, codeParsed);
            }
        }
        System.out.println("********** DONE ***********");
    }

    private static void processDirectory(JParser jParser, CodeParser wrapper, CustomFileDescriptor fileSourceDir, CustomFileDescriptor fileGenDir, String[] excludes, CustomFileDescriptor dir) throws Exception {
        CustomFileDescriptor[] files = dir.list();
        for(CustomFileDescriptor file : files) {
            if(file.isDirectory()) {
                if(file.path().contains(".svn")) continue;
                if(file.path().contains(".git")) continue;
                processDirectory(jParser, wrapper, fileSourceDir, fileGenDir, excludes, file);
            }
            else {
                if(file.extension().equals("java")) {
                    boolean stop = false;
                    if(excludes != null) {
                        for(int i = 0; i < excludes.length; i++) {
                            String path = file.path();
                            String exclude = excludes[i];

                            if(exclude.startsWith("!")) {
                                String substring = exclude.substring(1);
                                if(path.contains(substring)) {
                                    stop = false;
                                    break;
                                }
                            }
                            else if(path.contains(exclude)) {
                                stop = true;
                            }
                        }
                    }

                    if(stop)
                        continue;

                    String className = getFullyQualifiedClassName(fileSourceDir, file);
                    CustomFileDescriptor codeFile = new CustomFileDescriptor(fileGenDir + "/" + className + ".cpp");
                    if(file.lastModified() < codeFile.lastModified()) {
                        System.out.println("Code for '" + file.path() + "' up to date");
                        continue;
                    }
                    String javaContent = file.readString();
                    File file1 = file.file();
                    CompilationUnit unit = StaticJavaParser.parse(new ByteArrayInputStream(javaContent.getBytes()));
                    unit.printer(new CustomPrettyPrinter());
                    String absolutePath = file1.getAbsolutePath();

                    String packageFilePath = absolutePath.replace(fileSourceDir.file().getAbsolutePath(), "");
                    String fullPath = fileGenDir.file().getAbsolutePath();
                    fullPath = fullPath + packageFilePath;

                    jParser.unitArray.add(new JParserItem(unit, absolutePath, fullPath));
                }
            }
        }
    }

    private static void generateFile(String destinationPath, String javaContent) {
        CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(destinationPath);
        fileDescriptor.writeString(javaContent, false);
    }

    private static String getFullyQualifiedClassName(CustomFileDescriptor fileSourceDir, CustomFileDescriptor file) {
        String className = file.path().replace(fileSourceDir.path(), "").replace('\\', '.').replace('/', '.').replace(".java", "");
        if(className.startsWith(".")) className = className.substring(1);
        return className;
    }

    private static String parseJava(JParser jParser, CodeParser wrapper, JParserItem parserItem) throws Exception {
        ArrayList<Node> array = new ArrayList<>();
        CompilationUnit unit = parserItem.unit;
        array.addAll(unit.getChildNodes());
        PositionUtils.sortByBeginPosition(array, false);
        for(int i = 0; i < array.size(); i++) {
            Node node = array.get(i);
            if(node instanceof PackageDeclaration) {
                PackageDeclaration packageD = (PackageDeclaration)node;
                packageD.setComment(new BlockComment(gen));
            }

            CodeParserItem codeParserItem = createParserItem(unit, node);
            wrapper.parseCode(codeParserItem);
            if(node instanceof ClassOrInterfaceDeclaration) {
                if(node.getParentNode().isPresent()) {
                    ClassOrInterfaceDeclaration nodeInterface = (ClassOrInterfaceDeclaration)node;
                    parseClassInterface(jParser, unit, wrapper, nodeInterface);
                }
                else {
                    // Skip java file if there is no root class
                    return null;
                }
            }
        }
        wrapper.onParseCodeEnd();
        PositionUtils.sortByBeginPosition(unit.getTypes(), false);
        wrapper.onClassParsed(parserItem);
        return unit.toString();
    }

    private static void parseClassInterface(JParser jParser, CompilationUnit unit, CodeParser wrapper, ClassOrInterfaceDeclaration clazzInterface) {
        wrapper.onParseCodeEnd();
        wrapper.onParseClassStart(jParser, unit, clazzInterface);
        ArrayList<Node> array = new ArrayList<>();
        array.addAll(clazzInterface.getChildNodes());
        PositionUtils.sortByBeginPosition(array, false);

        for(int i = 0; i < array.size(); i++) {
            Node node = array.get(i);
            CodeParserItem parserItem = createParserItem(unit, node);
            wrapper.parseCode(parserItem);
            if(node instanceof ClassOrInterfaceDeclaration && node.getParentNode().isPresent()) {
                ClassOrInterfaceDeclaration nodeInterface = (ClassOrInterfaceDeclaration)node;
                parseClassInterface(jParser, unit, wrapper, nodeInterface);
            }
        }
        PositionUtils.sortByBeginPosition(clazzInterface.getMembers(), false);
        wrapper.onParseClassEnd(jParser, unit, clazzInterface);
    }

    private static CodeParserItem createParserItem(CompilationUnit unit, Node node) {
        CodeParserItem parserItem = new CodeParserItem();
        parserItem.unit = unit;
        parserItem.node = node;
        return parserItem;
    }
}