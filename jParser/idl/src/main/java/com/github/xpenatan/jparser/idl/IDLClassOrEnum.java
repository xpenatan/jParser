package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLClassOrEnum {

    public String name;
    public String subPackage;
    public final ArrayList<IDLLine> classLines = new ArrayList<>();

    public boolean isEnum() {
        return this instanceof IDLEnum;
    }

    public boolean isClass() {
        return this instanceof IDLClass;
    }

    public IDLClass asClass() {
        return (IDLClass)this;
    }

    public IDLEnum asEnum() {
        return (IDLEnum)this;
    }

    @Override
    public String toString() {
        return name;
    }

    protected void setupLines(ArrayList<String> lines) {
        for(int i = 0; i < lines.size(); i++) {
            String originalLine = lines.get(i);
            int commentIndex = originalLine.indexOf("//");
            if(commentIndex != -1) {
                String command = null;
                String code = originalLine.substring(0, commentIndex);
                String comment = originalLine.replace(code, "").replace("//", "").trim();
                code = code.trim();
                comment = comment.trim();
                if(comment.isEmpty()) {
                    comment = null;
                }
                else {
                    int startIdx = comment.indexOf("[-");
                    int endIdx = comment.indexOf("]");
                    if(startIdx != -1 && endIdx != -1 && endIdx > startIdx+2) {
                        String tempCommand = comment.substring(startIdx, endIdx+1);
                        comment = comment.replace(tempCommand, "").trim();
                        command = tempCommand.trim();
                        if(comment.isEmpty()) {
                            comment = null;
                        }
                    }
                }
                IDLLine idlLine = new IDLLine(code, comment, command);
                classLines.add(idlLine);
            }
            else {
                classLines.add(new IDLLine(originalLine, null, null));
            }
        }
    }


    IDLLine searchLine(String text, boolean startsWith) {
        for(int i = 0; i < classLines.size(); i++) {
            IDLLine idlLine = classLines.get(i);
            String line = idlLine.line;

            if(startsWith) {
                if(line.startsWith(text)) {
                    return idlLine;
                }
            }
            else {
                if(line.contains(text)) {
                    return idlLine;
                }
            }
        }
        return null;
    }
}