package com.github.xpenatan.jparser.idl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLReader {

    public final ArrayList<IDLFile> fileArray = new ArrayList<>();

    public final String cppDir;

    public IDLReader(String cppDir) {
        String srcPath = null;
        if(cppDir != null) {
            try {
                srcPath = new File(cppDir).getCanonicalPath() + File.separator;
            } catch(IOException e) {
            }
        }
        this.cppDir = srcPath;
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

    public static IDLReader readIDL(String idlDir) {
        return readIDL(idlDir, null);
    }

    public static IDLReader readIDL(String idlDir, String cppDir) {
        IDLReader reader = new IDLReader(cppDir);
        IDLFile idlFile = parseFile(idlDir);
        if(idlFile != null) {
            reader.fileArray.add(idlFile);
        }
        return reader;
    }

    public static IDLFile parseFile(String path) {
        path = path.replace("\\", File.separator);
        File file = new File(path);    //creates a new file instance
        if(file.exists()) {
            try {
                InputStreamReader fr = new FileReader(file);
                return parseFile(fr);
            } catch(FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException("IDL file does not exist: " + path);
        }
    }

    public static IDLFile parseFile(InputStreamReader inputStreamReader) {
        IDLFile idlFile = new IDLFile();
            try {
                BufferedReader br = new BufferedReader(inputStreamReader);  //creates a buffering character input stream
                ArrayList<String> lines = new ArrayList<>();
                String line;
                while((line = br.readLine()) != null) {
                    lines.add(line);
                }
                inputStreamReader.close();    //closes the stream and release the resources

                ArrayList<IDLClass> classList = new ArrayList<>();
                parseFile(idlFile, lines, classList);
                idlFile.classArray.addAll(classList);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        return idlFile;
    }

    private static void parseFile(IDLFile idlFile, ArrayList<String> lines, ArrayList<IDLClass> classList) {
        ArrayList<String> classLines = new ArrayList<>();
        boolean foundStartClass = false;
        int size = lines.size();
        for(int i = 0; i < size; i++) {
            String line = lines.get(i).trim();
            if(line.startsWith("//") || line.isEmpty())
                continue;

            boolean justAdded = false;

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
                        String nextLine = lines.get(nextLineIdx);
                        if(nextLine.contains(" implements ")) {
                            classLines.add(nextLine.trim());
                            i++; // add i so nextLine is not skipped on next loop
                        }
                    }
                    foundStartClass = false;
                    IDLClass parserLineClass = new IDLClass(idlFile);
                    parserLineClass.initClass(classLines);
                    classLines.clear();
                    classList.add(parserLineClass);
                }
            }
        }
    }
}