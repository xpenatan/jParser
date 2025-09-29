package com.github.xpenatan.jparser.idl;

import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLClassHeader {
    public final IDLClass idlClass;

    public String headerLine;

    public String prefixName = "";
    public String jsImplementation;
    public boolean isNoDelete;

    public ArrayList<String> options = new ArrayList<>();

    public IDLClassHeader(String headerLine, IDLClass idlClass) {
        this.headerLine = headerLine;
        this.idlClass = idlClass;

        if(!headerLine.isEmpty()) {
            if(headerLine.startsWith("[") && headerLine.endsWith("]")) {
                headerLine = headerLine.replace("[", "").replace("]", "");
                if(headerLine.contains(",")) {
                    String[] commaSplit = headerLine.split(",");
                    for(String option : commaSplit) {
                        options.add(option.trim());
                    }
                }
                else {
                    options.add(headerLine);
                }

                setupPrefixName();
                setupJSImplementation();
                setupNoDelete();
            }
            else {
                throw new RuntimeException("Wrong header: " + headerLine);
            }
        }
    }

    public void setupPrefixName() {
        for(String option : options) {
            if(option.startsWith("Prefix")) {
                String[] split = option.split("=");
                prefixName = split[1].replace("\"", "").trim();
            }
        }
    }

    private void setupJSImplementation() {
        for(String option : options) {
            if(option.startsWith("JSImplementation")) {
                String[] split = option.split("=");
                jsImplementation = split[1].replace("\"", "").trim();
            }
        }
    }

    private void setupNoDelete() {
        for(String option : options) {
            if(option.equals("NoDelete")) {
                isNoDelete = true;
            }
        }
    }

    public static boolean isLineHeader(String line) {
        return line.startsWith("[Prefix") || line.startsWith("[JSImplementation") || line.startsWith("[NoDelete");
    }
}