package com.github.xpenatan.jparser.idl;

/**
 * @author xpenatan
 */
public class IDLAttribute {
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
        if(leftSide.contains("[Value]")) {
            isValue = true;
        }
        if(leftSide.contains("[Const]")) {
            isConst = true;
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