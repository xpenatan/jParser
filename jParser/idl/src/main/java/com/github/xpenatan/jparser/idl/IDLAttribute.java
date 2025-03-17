package com.github.xpenatan.jparser.idl;

/**
 * @author xpenatan
 */
public class IDLAttribute {
    public IDLFile idlFile;
    public IDLLine idlLine;
    public String idlType;
    public String name;
    public boolean skip = false;
    public boolean isAny = false;
    public boolean isStatic = false;
    public boolean isReadOnly = false;
    public boolean isConst = false;
    public boolean isValue = false;
    public boolean isArray = false;
    public IDLClassOrEnum idlClassOrEnum;

    public IDLAttribute(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initAttribute(IDLLine idlLine) {
        this.idlLine = idlLine;
        String line = idlLine.line;

        String[] split = line.split("attribute");
        String leftSide = split[0].trim();
        String rightSide = split[1].trim();
        String[] rightSlideSplit = rightSide.split(" ");
        String name = rightSlideSplit[rightSlideSplit.length-1];
        idlType = rightSide.replace(name, "").trim();
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

        if(idlType.contains("[]")) {
            isArray = true;
            idlType = idlType.replace("[]", "").trim();
            idlType = idlType + "[]";
        }

        if(idlType.equals("any")) {
            isAny = true;
        }

        if(isAny) {
            //TODO improve
            skip = true;
        }
    }

    public String getCPPType() {
        //Attributes don't set/get arrays so we remove it
        String fullType = idlType;
        if(idlClassOrEnum != null && idlClassOrEnum.isClass()) {
            IDLClass aClass = idlClassOrEnum.asClass();
            fullType = aClass.getCPPName();
        }
        return IDLHelper.getCPPReturnType(fullType).replace("[]", "");
    }

    public String getJavaType() {
        //Attributes don't set/get arrays so we remove it
        return IDLHelper.getJavaType(false, idlType).replace("[]", "");
    }
}