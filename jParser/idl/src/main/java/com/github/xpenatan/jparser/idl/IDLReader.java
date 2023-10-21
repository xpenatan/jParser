package com.github.xpenatan.jparser.idl;

import com.github.xpenatan.jparser.core.JParser;
import idl.IDLBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLReader {

    public final ArrayList<IDLFile> fileArray = new ArrayList<>();

    public IDLReader() {

        if(JParser.CREATE_IDL_HELPER) {
            String baseIDLPath = "IDLHelper.idl";
            InputStream resourceAsStream = IDLBase.class.getClassLoader().getResourceAsStream(baseIDLPath);
            InputStreamReader streamReader = new InputStreamReader(resourceAsStream);
            IDLFile baseIDLFile = IDLReader.parseFile(streamReader, baseIDLPath);
            fileArray.add(baseIDLFile);
        }
    }

    public String mergeIDLFiles() {
        String idlFinalStr = "";
        for(IDLFile idlFile : fileArray) {
            String idlStr = idlFile.getIDLStr();
            idlFinalStr += "\n// " + idlFile.idlName + "\n\n";
            idlFinalStr += idlStr;
        }
        return idlFinalStr;
    }

    public IDLClass getClass(String name) {
        for(int i = 0; i < fileArray.size(); i++) {
            IDLFile idlFile = fileArray.get(i);
            IDLClass idlClass = idlFile.getClass(name);
            if(idlClass != null) {
                return idlClass;
            }
        }
        return null;
    }

    public IDLEnum getEnum(String name) {
        for(int i = 0; i < fileArray.size(); i++) {
            IDLFile idlFile = fileArray.get(i);
            IDLEnum idlEnum = idlFile.getEnum(name);
            if(idlEnum != null) {
                return idlEnum;
            }
        }
        return null;
    }

    public static IDLReader readIDL(String idlDir) {
        IDLReader reader = new IDLReader();
        try {
            idlDir = new File(idlDir).getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException("IDL file not found: " + idlDir);
        }
        IDLFile idlFile = parseFile(idlDir);
        reader.fileArray.add(idlFile);
        return reader;
    }

    public static void addIDL(IDLReader reader, String idlDir) {
        try {
            idlDir = new File(idlDir).getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException("IDL file not found: " + idlDir);
        }
        IDLFile idlFile = parseFile(idlDir);
        reader.fileArray.add(idlFile);
    }

    public static IDLReader readIDL(ArrayList<String> idlDirs) {
        IDLReader reader = new IDLReader();
        for(String idlDir : idlDirs) {
            IDLFile idlFile = parseFile(idlDir);
            reader.fileArray.add(idlFile);
        }
        return reader;
    }

    public static IDLFile parseFile(String path) {
        path = path.replace("\\", File.separator);
        File file = new File(path);    //creates a new file instance
        if(file.exists()) {
            String name = file.getName();
            try {
                InputStreamReader fr = new FileReader(file);
                return parseFile(fr, name);
            } catch(FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException("IDL file does not exist: " + path);
        }
    }

    public static IDLFile parseFile(InputStreamReader inputStreamReader, String idlName) {
        IDLFile idlFile = new IDLFile(idlName);
            try {
                BufferedReader br = new BufferedReader(inputStreamReader);  //creates a buffering character input stream
                ArrayList<String> lines = new ArrayList<>();
                String line;
                while((line = br.readLine()) != null) {
                    lines.add(line);
                }
                inputStreamReader.close();    //closes the stream and release the resources

                idlFile.lines.addAll(lines);
                ArrayList<IDLClassOrEnum> classList = new ArrayList<>();
                parseFile(idlFile, classList);
                idlFile.classArray.addAll(classList);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        return idlFile;
    }

    private static void parseFile(IDLFile idlFile, ArrayList<IDLClassOrEnum> classList) {
        ArrayList<String> classLines = new ArrayList<>();
        boolean foundStartClass = false;
        boolean foundStartEnum = false;
        ArrayList<String> settings = new ArrayList<>();
        int size = idlFile.lines.size();
        for(int i = 0; i < size; i++) {
            String line = idlFile.lines.get(i).trim();
            if(line.startsWith("//") || line.isEmpty()) {
                if(foundStartClass || foundStartEnum) {
                    String cmd = line.replace("//", "").trim();
                    if(cmd.startsWith("[-") && cmd.endsWith("]")) {
                        settings.add(cmd);
                    }
                }
                continue;
            }

            boolean justAdded = false;

            if(!foundStartEnum) {
                if(line.startsWith("enum ")) {
                    foundStartEnum = true;
                    classLines.clear();
                }
            }

            if(foundStartEnum) {
                classLines.add(line);

                if(line.endsWith("};")) {
                    foundStartEnum = false;
                    IDLEnum parserLineEnum = new IDLEnum(idlFile);
                    parserLineEnum.settings.addAll(settings);
                    parserLineEnum.initEnum(classLines);
                    classLines.clear();
                    classList.add(parserLineEnum);
                    settings.clear();
                }
            }

            if(!foundStartClass) {
                if(line.startsWith("interface ") || IDLClassHeader.isLineHeader(line)) {
                    foundStartClass = true;
                    classLines.clear();
                    justAdded = true;
                    classLines.add(line);
                }
            }
            if(foundStartClass) {
                if(!justAdded) {
                    classLines.add(line);
                }
                if(line.endsWith("};")) {
                    int nextLineIdx = i + 1;
                    if(nextLineIdx < size) {
                        String nextLine = idlFile.lines.get(nextLineIdx);
                        if(nextLine.contains(" implements ")) {
                            classLines.add(nextLine.trim());
                            i++; // add i so nextLine is not skipped on next loop
                        }
                    }
                    foundStartClass = false;
                    IDLClass parserLineClass = new IDLClass(idlFile);
                    parserLineClass.settings.addAll(settings);
                    parserLineClass.initClass(classLines);
                    classLines.clear();
                    classList.add(parserLineClass);
                    settings.clear();
                }
            }
        }
    }
}