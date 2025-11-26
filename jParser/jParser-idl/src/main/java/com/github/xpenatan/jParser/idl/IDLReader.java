package com.github.xpenatan.jParser.idl;

import com.github.xpenatan.jParser.core.JParser;
import idl.helper.IDLArray;
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
            fileArray.add(getIDLHelperFile());
        }
    }

    public static IDLFile getIDLHelperFile() {
        String baseIDLPath = "IDLHelper.idl";
        InputStream resourceAsStream = IDLArray.class.getClassLoader().getResourceAsStream(baseIDLPath);
        InputStreamReader streamReader = new InputStreamReader(resourceAsStream);
        IDLFile baseIDLFile = IDLReader.parseFile(streamReader, baseIDLPath);
        return baseIDLFile;
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

    public IDLEnumClass getEnum(String name) {
        for(int i = 0; i < fileArray.size(); i++) {
            IDLFile idlFile = fileArray.get(i);
            IDLEnumClass idlEnum = idlFile.getEnum(name);
            if(idlEnum != null) {
                return idlEnum;
            }
        }
        return null;
    }

    public IDLClassOrEnum getClassOrEnum(String name) {
        for(int i = 0; i < fileArray.size(); i++) {
            IDLFile idlFile = fileArray.get(i);
            IDLClass idlClass = idlFile.getClass(name);
            if(idlClass != null) {
                return idlClass;
            }
            IDLEnumClass idlEnum = idlFile.getEnum(name);
            if(idlEnum != null) {
                return idlEnum;
            }
        }
        return null;
    }

    public ArrayList<IDLClassOrEnum> getAllClasses() {
        ArrayList<IDLClassOrEnum> classes = new ArrayList<>();
        for(int i = 0; i < fileArray.size(); i++) {
            IDLFile idlFile = fileArray.get(i);
            classes.addAll(idlFile.classArray);
        }
        return classes;
    }

    public static IDLReader readIDL(String ...idlDirs) {
        IDLReader reader = new IDLReader();

        for(int i = 0; i < idlDirs.length; i++) {
            String idlDir = idlDirs[i];
            try {
                idlDir = new File(idlDir).getCanonicalPath() + File.separator;
                IDLFile idlFile = parseFile(idlDir);
                reader.fileArray.add(idlFile);
            } catch(IOException e) {
                throw new RuntimeException("IDL file not found: " + idlDir);
            }
        }

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

    public static void addIDLRef(IDLReader reader, String idlDir) {
        try {
            idlDir = new File(idlDir).getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException("IDL file not found: " + idlDir);
        }
        IDLFile idlFile = parseFile(idlDir);
        idlFile.skip = true;
        reader.fileArray.add(idlFile);
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
            String originalLine = idlFile.lines.get(i).trim();
            String line = removeComment(originalLine);
            String nextLine = getNextLine(idlFile.lines, i);

            if(line.isEmpty()) {
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
                if(line.startsWith("enum ") || IDLClassHeader.isLineHeader(line) && nextLine.startsWith("enum ")) {
                    foundStartEnum = true;
                    classLines.clear();
                }
            }

            if(foundStartEnum) {
                classLines.add(originalLine);

                if(line.endsWith("};")) {
                    foundStartEnum = false;
                    IDLEnumClass parserLineEnum = new IDLEnumClass(idlFile);
                    parserLineEnum.settings.addAll(settings);
                    parserLineEnum.initEnum(classLines);
                    classLines.clear();
                    classList.add(parserLineEnum);
                    settings.clear();
                }
            }

            if(!foundStartClass) {
                if(line.startsWith("interface ") || IDLClassHeader.isLineHeader(line) && nextLine.startsWith("interface ")) {
                    foundStartClass = true;
                    classLines.clear();
                    justAdded = true;
                    classLines.add(originalLine);
                }
            }
            if(foundStartClass) {
                if(!justAdded) {
                    classLines.add(originalLine);
                }
                if(line.endsWith("};")) {
                    String nextL = getNextLine(idlFile.lines, i);;
                    if(nextL.contains(" implements ")) {
                        classLines.add(nextL.trim());
                        i++; // add i so nextLine is not skipped on next loop
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

    public static void setupClasses(IDLReader idlReader) {
        ArrayList<IDLClassOrEnum> classList = idlReader.getAllClasses();
        configClassType(idlReader, classList);
        configCallbacks(idlReader, classList);
    }

    private static void configClassType(IDLReader idlReader, ArrayList<IDLClassOrEnum> classList) {
        // Setup all attributes and parameters type
        for(int i = 0; i < classList.size(); i++) {
            IDLClassOrEnum idlClassOrEnum = classList.get(i);
            if(idlClassOrEnum.isClass()) {
                IDLClass idlClass = idlClassOrEnum.asClass();
                for(IDLConstructor constructor : idlClass.constructors) {
                    for(IDLParameter parameter : constructor.parameters) {
                        String idlType = parameter.idlType.replace("[]", "");
                        IDLClassOrEnum childClassOrEnum = idlReader.getClassOrEnum(idlType);
                        parameter.idlClassOrEnum = childClassOrEnum;
                    }
                }
                for(IDLAttribute attribute : idlClass.attributes) {
                    String idlType = attribute.idlType;
                    IDLClassOrEnum childClassOrEnum = idlReader.getClassOrEnum(idlType);
                    attribute.idlClassOrEnum = childClassOrEnum;
                }
                for(IDLMethod method : idlClass.methods) {
                    for(IDLParameter parameter : method.parameters) {
                        String idlType = parameter.idlType.replace("[]", "");
                        IDLClassOrEnum childClassOrEnum = idlReader.getClassOrEnum(idlType);
                        parameter.idlClassOrEnum = childClassOrEnum;
                    }
                    String returnType = method.returnType;
                    IDLClassOrEnum childClassOrEnum = idlReader.getClassOrEnum(returnType);
                    method.returnClassType = childClassOrEnum;
                }
            }
        }
    }

    private static void configCallbacks(IDLReader idlReader, ArrayList<IDLClassOrEnum> classList) {
        for(int i = 0; i < classList.size(); i++) {
            IDLClassOrEnum idlClassOrEnum = classList.get(i);
            if(idlClassOrEnum.isClass()) {
                IDLClass idlCallbackImpl = idlClassOrEnum.asClass();
                String jsImplementation = idlCallbackImpl.classHeader.jsImplementation;
                if(jsImplementation != null) {
                    jsImplementation = jsImplementation.trim();
                    if(!jsImplementation.isEmpty()) {
                        IDLClass callbackClass = idlReader.getClass(jsImplementation);
                        if(callbackClass != null) {
                            if(callbackClass.callbackImpl == null) {
                                callbackClass.callbackImpl = idlCallbackImpl;
                                idlCallbackImpl.isCallback = true;
                            }
                            else {
                                throw new RuntimeException("Class " + callbackClass.name + " cannot have multiple JSImplementation");
                            }
                        }
                    }
                }
            }
        }
    }

    private static String removeComment(String line) {
        int commentIndex = line.indexOf("//");
        if(commentIndex != -1) {
            line = line.substring(0, commentIndex);
            line = line.trim();
        }
        return line;
    }

    private static String getNextLine(ArrayList<String> lines, int index) {
        String nextLine = "";
        int size = lines.size();
        if(index + 1 < size) {
            nextLine = lines.get(index+1).trim();
            nextLine = removeComment(nextLine);
            if(nextLine.isEmpty() && index + 2 < size) {
                nextLine = lines.get(index+2).trim();
                nextLine = removeComment(nextLine);
            }
        }
        return nextLine;
    }
}