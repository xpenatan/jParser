package com.github.xpenatan.jParser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLClass extends IDLClassOrEnum {
    public final IDLFile idlFile;

    public IDLClassHeader classHeader;

    public String extendClass = "";
    public final ArrayList<IDLConstructor> constructors = new ArrayList<>();
    public final ArrayList<IDLMethod> methods = new ArrayList<>();
    public final ArrayList<IDLAttribute> attributes = new ArrayList<>();
    public ArrayList<String> settings = new ArrayList<>();

    public boolean isCallback;
    public IDLClass callbackImpl;

    public IDLClass(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initClass(ArrayList<String> lines) {
        setupLines(lines);
        setupHeader();
        setupInterfaceName();
        setupInterfacePackage();
        setupExtendClass();
        setupAttributesAndMethods();

        IDLLine idlLine = searchLine("interface ", true);
        if(idlLine != null) {
            idlSkip = idlLine.idlCommand.containsCommand(IDLCommand.CMD_SKIP);
        }
    }

    private void setupAttributesAndMethods() {
        for(int i = 1; i < classLines.size(); i++) {
            IDLLine idlLine = classLines.get(i);
            String line = idlLine.line;
            if(line.contains("attribute ")) {
                IDLAttribute attribute = new IDLAttribute(idlFile);
                attribute.initAttribute(idlLine);
                attributes.add(attribute);
            }
            else {
                if(line.contains("void " + name)) {
                    IDLConstructor constructor = new IDLConstructor(idlFile, this);
                    constructor.initConstructor(idlLine);
                    constructors.add(constructor);

                    int totalOptionalParams = constructor.getTotalOptionalParams();
                    if(totalOptionalParams > 0) {
                        for(int j = 0; j < totalOptionalParams; j++) {
                            IDLConstructor clone = constructor.clone();
                            clone.removeLastParam(j + 1);
                            constructors.add(clone);
                        }
                    }
                }
                else {
                    if(line.contains("(") && line.contains(")")) {
                        IDLMethod method = new IDLMethod(this, idlFile);
                        method.initMethod(idlLine);
                        methods.add(method);

                        int totalOptionalParams = method.getTotalOptionalParams();
                        if(totalOptionalParams > 0) {
                            for(int j = 0; j < totalOptionalParams; j++) {
                                IDLMethod clone = method.clone();
                                clone.removeLastParam(j + 1);
                                methods.add(clone);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupInterfaceName() {
        IDLLine idlLine = searchLine("interface ", true);
        if(idlLine != null) {
            name = idlLine.line.split(" ")[1].trim();
        }
    }

    private void setupInterfacePackage() {
        IDLLine idlLine = searchLine("interface ", true);
        if(idlLine != null && idlLine.idlCommand.containsCommand(IDLCommand.CMD_SUB_PACKAGE)) {
            subPackage = idlLine.idlCommand.getCommandValue(IDLCommand.CMD_SUB_PACKAGE);
        }
    }

    private void setupHeader() {
        String code = "";
        if(classLines.size() > 0) {
            IDLLine idlLine = classLines.get(0);
            String headerLine = idlLine.line;
            if(IDLClassHeader.isLineHeader(headerLine)) {
                code = headerLine;
            }
        }
        classHeader = new IDLClassHeader(code, this);
    }

    private void setupExtendClass() {
        IDLLine idlLine = searchLine(" implements ", false);
        if(idlLine != null && !idlLine.line.startsWith("//")) {
            String[] split = idlLine.line.split("implements");
            extendClass = split[1].trim().replace(";", "");
        }
        if(extendClass.isEmpty()) {
            // If implements is not found check for :
            IDLLine interfaceLine = searchLine("interface ", true);
            if(interfaceLine != null && interfaceLine.line.contains(":")) {
                String[] colonSplit = interfaceLine.line.split(":");
                String[] spaceSplit = colonSplit[1].trim().split(" ");
                extendClass = spaceSplit[0].trim();
            }
        }
    }

    public String getCPPName() {
        return classHeader.prefixName + name;
    }

    public IDLMethod getMethod(String methodName) {
        for(IDLMethod method : methods) {
            if(method.nameEquals(methodName)) {
                return method;
            }
        }
        return null;
    }

}