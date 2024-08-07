package com.github.xpenatan.jparser.idl;

/**
 * @author xpenatan
 */
public class IDLAttribute {
    public IDLFile idlFile;
    public String line;
    public String type;
    public String name;
    public boolean skip = false;
    public boolean isAny = false;
    public boolean isStatic = false;
    public boolean isReadOnly = false;
    public boolean isConst = false;
    public boolean isValue = false;
    public boolean isArray = false;

    public IDLAttribute(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initAttribute(String line) {
        this.line = line;

        String[] split = line.split("attribute");
        String leftSide = split[0].trim();
        String rightSide = split[1].trim();
        String[] rightSlideSplit = rightSide.split(" ");
        String name = rightSlideSplit[rightSlideSplit.length-1];
        type = rightSide.replace(name, "").trim();
        this.name = name.replace(";", "").trim();

        if(leftSide.contains("static")) {
            isStatic = true;
        }

        String tagsStr = IDLHelper.getTags(leftSide);
        if(!tagsStr.isEmpty()) {
            if(tagsStr.contains("Value")) {
                isValue = true;
            }
            if(leftSide.contains("Const")) {
                isConst = true;
            }
        }
        if(leftSide.contains("readonly")) {
            isReadOnly = true;
        }

        if(type.contains("[]")) {
            isArray = true;
            type = type.replace("[]", "").trim();
            type = type+"[]";
        }

        if(type.endsWith("long") || type.endsWith("long[]")) {
            type = type.replace("long", "int");
        }

        if(type.contains("int int")) {
            type = type.replace("int int", "long");
        }

        if(type.contains("unsigned")) {
            type = type.replace("unsigned", "").trim();
        }

        if(type.equals("any")) {
            isAny = true;
        }

        if(isAny) {
            //TODO improve
            skip = true;
        }
    }
}