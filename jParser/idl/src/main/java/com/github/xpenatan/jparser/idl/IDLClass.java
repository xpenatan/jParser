package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLClass extends IDLClassOrEnum {
    public final IDLFile idlFile;

    public IDLClassHeader classHeader;

    public String extendClass = "";
    public final ArrayList<String> classLines = new ArrayList<>();
    public final ArrayList<IDLConstructor> constructors = new ArrayList<>();
    public final ArrayList<IDLMethod> methods = new ArrayList<>();
    public final ArrayList<IDLAttribute> attributes = new ArrayList<>();
    public ArrayList<String> settings = new ArrayList<>();

    public boolean isCallback;
    public IDLClass callbackImpl;
    public boolean idlSkip = false;

    public IDLClass(IDLFile idlFile) {
        this.idlFile = idlFile;
    }

    public void initClass(ArrayList<String> lines) {
        classLines.addAll(lines);
        setupHeader();
        setupInterfaceName();
        setupExtendClass();
        setupAttributesAndMethods();
    }

    private void setupAttributesAndMethods() {
        for(int i = 1; i < classLines.size(); i++) {
            String line = classLines.get(i);
            if(line.contains("attribute ")) {
                IDLAttribute attribute = new IDLAttribute(idlFile);
                attribute.initAttribute(line);
                attributes.add(attribute);
            }
            else {
                if(line.contains("void " + name)) {
                    IDLConstructor constructor = new IDLConstructor(idlFile, this);
                    constructor.initConstructor(line);
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
                        method.initMethod(line);
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
        String line = searchLine("interface ", true, false);
        if(line != null) {
            name = line.split(" ")[1];
        }
    }

    private void setupHeader() {
        String line = "";
        if(classLines.size() > 0) {
            String headerLine = classLines.get(0);
            if(IDLClassHeader.isLineHeader(headerLine)) {
                line = headerLine;
            }
        }
        classHeader = new IDLClassHeader(line, this);
    }

    private void setupExtendClass() {
        String line = searchLine(" implements ", false, false);
        if(line != null && !line.startsWith("//")) {
            String[] split = line.split("implements");
            extendClass = split[1].trim().replace(";", "");
        }
        if(extendClass.isEmpty()) {
            // If implements is not found check for :
            String interfaceLine = searchLine("interface ", true, false);
            if(interfaceLine != null && interfaceLine.contains(":")) {
                String[] colonSplit = interfaceLine.split(":");
                String[] spaceSplit = colonSplit[1].trim().split(" ");
                extendClass = spaceSplit[0].trim();
            }
        }
    }

    private String searchLine(String text, boolean startsWith, boolean endsWith) {
        for(int i = 0; i < classLines.size(); i++) {
            String line = classLines.get(i);

            if(startsWith) {
                if(line.startsWith(text)) {
                    return line;
                }
            }
            else if(endsWith) {
                if(line.endsWith(text)) {
                    return line;
                }
            }
            else {
                if(line.contains(text)) {
                    return line;
                }
            }
        }
        return null;
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