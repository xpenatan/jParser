package com.github.xpenatan.jParser.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Represents a file or directory on the filesystem or classpath. Taken from libgdx's FileHandle.
 *
 * @author mzechner
 * @author Nathan Sweet
 */
public class CustomFileDescriptor {
    /**
     * Indicates how to resolve a path to a file.
     *
     * @author mzechner
     * @author Nathan Sweet
     */
    public enum FileType {
        /**
         * Path relative to the root of the classpath. Classpath files are always readonly. Note that classpath files are not
         * compatible with some functionality on Android, such as Audio#newSound(FileHandle) and Audio#newMusic(FileHandle).
         */
        Classpath,

        /**
         * Path that is a fully qualified, absolute filesystem path. To ensure portability across platforms use absolute files only
         * when absolutely (heh) necessary.
         */
        Absolute;
    }

    protected File file;
    protected FileType type;

    protected CustomFileDescriptor() {
    }

    public CustomFileDescriptor(String fileName) {
        this.file = new File(fileName);
        this.type = FileType.Absolute;
    }

    public CustomFileDescriptor(File file) {
        this.file = file;
        this.type = FileType.Absolute;
    }

    public CustomFileDescriptor(String fileName, FileType type) {
        this.type = type;
        file = new File(fileName);
    }

    protected CustomFileDescriptor(File file, FileType type) {
        this.file = file;
        this.type = type;
    }

    public String path() {
        return file.getPath().replace('\\', '/');
    }

    public String name() {
        return file.getName();
    }

    public String extension() {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1) return "";
        return name.substring(dotIndex + 1);
    }

    public String nameWithoutExtension() {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1) return name;
        return name.substring(0, dotIndex);
    }

    public FileType type() {
        return type;
    }

    public File file() {
        return file;
    }

    public InputStream read() {
        if(type == FileType.Classpath && !file.exists()) {
            InputStream input = CustomFileDescriptor.class.getResourceAsStream("/" + file.getPath().replace('\\', '/'));
            if(input == null) throw new RuntimeException("File not found: " + file + " (" + type + ")");
            return input;
        }
        try {
            return new FileInputStream(file());
        }
        catch(FileNotFoundException ex) {
            if(file().isDirectory())
                throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
            throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
        }
    }

    public Reader reader() {
        return new InputStreamReader(read());
    }

    public Reader reader(String charset) {
        try {
            return new InputStreamReader(read(), charset);
        }
        catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("Error reading file: " + this, ex);
        }
    }

    public BufferedReader reader(int bufferSize) {
        return new BufferedReader(new InputStreamReader(read()), bufferSize);
    }

    public BufferedReader reader(int bufferSize, String charset) {
        try {
            return new BufferedReader(new InputStreamReader(read(), charset), bufferSize);
        }
        catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("Error reading file: " + this, ex);
        }
    }

    public String readString() {
        return readString(null);
    }

    public String readString(String charset) {
        StringBuilder output = new StringBuilder(512);
        InputStreamReader reader = null;
        try {
            if(charset == null)
                reader = new InputStreamReader(read());
            else
                reader = new InputStreamReader(read(), charset);
            char[] buffer = new char[256];
            while(true) {
                int length = reader.read(buffer);
                if(length == -1) break;
                output.append(buffer, 0, length);
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Error reading layout file: " + this, ex);
        }
        finally {
            try {
                if(reader != null) reader.close();
            }
            catch(IOException ignored) {
            }
        }
        return output.toString();
    }

    public byte[] readBytes() {
        int length = (int)length();
        if(length == 0) length = 512;
        byte[] buffer = new byte[length];
        int position = 0;
        InputStream input = read();
        try {
            while(true) {
                int count = input.read(buffer, position, buffer.length - position);
                if(count == -1) break;
                position += count;
                if(position == buffer.length) {
                    // Grow buffer.
                    byte[] newBuffer = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, newBuffer, 0, position);
                    buffer = newBuffer;
                }
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Error reading file: " + this, ex);
        }
        finally {
            try {
                if(input != null) input.close();
            }
            catch(IOException ignored) {
            }
        }
        if(position < buffer.length) {
            // Shrink buffer.
            byte[] newBuffer = new byte[position];
            System.arraycopy(buffer, 0, newBuffer, 0, position);
            buffer = newBuffer;
        }
        return buffer;
    }

    public int readBytes(byte[] bytes, int offset, int size) {
        InputStream input = read();
        int position = 0;
        try {
            while(true) {
                int count = input.read(bytes, offset + position, size - position);
                if(count <= 0) break;
                position += count;
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Error reading file: " + this, ex);
        }
        finally {
            try {
                if(input != null) input.close();
            }
            catch(IOException ignored) {
            }
        }
        return position - offset;
    }

    public OutputStream write(boolean append) {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot write to a classpath file: " + file);
        parent().mkdirs();
        try {
            return new FileOutputStream(file(), append);
        }
        catch(FileNotFoundException ex) {
            if(file().isDirectory())
                throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
            throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
        }
    }

    public void write(InputStream input, boolean append) {
        OutputStream output = null;
        try {
            output = write(append);
            byte[] buffer = new byte[4096];
            while(true) {
                int length = input.read(buffer);
                if(length == -1) break;
                output.write(buffer, 0, length);
            }
        }
        catch(Exception ex) {
            throw new RuntimeException("Error stream writing to file: " + file + " (" + type + ")", ex);
        }
        finally {
            try {
                if(input != null) input.close();
            }
            catch(Exception ignored) {
            }
            try {
                if(output != null) output.close();
            }
            catch(Exception ignored) {
            }
        }
    }

    public Writer writer(boolean append) {
        return writer(append, null);
    }

    public Writer writer(boolean append, String charset) {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot write to a classpath file: " + file);
        parent().mkdirs();
        try {
            FileOutputStream output = new FileOutputStream(file(), append);
            if(charset == null)
                return new OutputStreamWriter(output);
            else
                return new OutputStreamWriter(output, charset);
        }
        catch(IOException ex) {
            if(file().isDirectory())
                throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
            throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
        }
    }

    public void writeString(String string, boolean append) {
        writeString(string, append, null);
    }

    public void writeString(String string, boolean append, String charset) {
        Writer writer = null;
        try {
            writer = writer(append, charset);
            writer.write(string);
        }
        catch(Exception ex) {
            throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
        }
        finally {
            try {
                if(writer != null) writer.close();
            }
            catch(Exception ignored) {
            }
        }
    }

    public void writeBytes(byte[] bytes, boolean append) {
        OutputStream output = write(append);
        try {
            output.write(bytes);
        }
        catch(IOException ex) {
            throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
        }
        finally {
            try {
                output.close();
            }
            catch(IOException ignored) {
            }
        }
    }

    public CustomFileDescriptor[] list() {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot list a classpath directory: " + file);
        String[] relativePaths = file().list();
        if(relativePaths == null) return new CustomFileDescriptor[0];
        CustomFileDescriptor[] handles = new CustomFileDescriptor[relativePaths.length];
        for(int i = 0, n = relativePaths.length; i < n; i++)
            handles[i] = child(relativePaths[i]);
        return handles;
    }

    public CustomFileDescriptor[] list(String suffix) {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot list a classpath directory: " + file);
        String[] relativePaths = file().list();
        if(relativePaths == null) return new CustomFileDescriptor[0];
        CustomFileDescriptor[] handles = new CustomFileDescriptor[relativePaths.length];
        int count = 0;
        for(int i = 0, n = relativePaths.length; i < n; i++) {
            String path = relativePaths[i];
            if(!path.endsWith(suffix)) continue;
            handles[count] = child(path);
            count++;
        }
        if(count < relativePaths.length) {
            CustomFileDescriptor[] newHandles = new CustomFileDescriptor[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    public boolean isDirectory() {
        if(type == FileType.Classpath) return false;
        return file().isDirectory();
    }

    public CustomFileDescriptor child(String name) {
        if(file.getPath().length() == 0) return new CustomFileDescriptor(new File(name), type);
        return new CustomFileDescriptor(new File(file, name), type);
    }

    public CustomFileDescriptor parent() {
        File parent = file.getParentFile();
        if(parent == null) {
            if(type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new CustomFileDescriptor(parent, type);
    }

    public boolean mkdirs() {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot mkdirs with a classpath file: " + file);
        return file().mkdirs();
    }

    public boolean exists() {
        if(type == FileType.Classpath)
            return CustomFileDescriptor.class.getResource("/" + file.getPath().replace('\\', '/')) != null;
        return file().exists();
    }

    public boolean delete() {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot delete a classpath file: " + file);
        return file().delete();
    }

    public boolean deleteDirectory() {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot delete a classpath file: " + file);
        return deleteDirectory(file());
    }

    public void copyTo(CustomFileDescriptor dest, boolean createChildFolder) {
        if(!isDirectory()) {
            if(dest.isDirectory()) dest = dest.child(name());
            copyFile(this, dest);
            return;
        }
        if(dest.exists()) {
            if(!dest.isDirectory()) throw new RuntimeException("Destination exists but is not a directory: " + dest);
        }
        else {
            dest.mkdirs();
            if(!dest.isDirectory()) throw new RuntimeException("Destination directory cannot be created: " + dest);
        }
        if(createChildFolder) {
            dest = dest.child(name());
        }
        copyDirectory(this, dest);
    }

    public void moveTo(CustomFileDescriptor dest) {
        if(type == FileType.Classpath) throw new RuntimeException("Cannot move a classpath file: " + file);
        copyTo(dest, true);
        delete();
    }

    public long length() {
        if(type == FileType.Classpath || !file.exists()) {
            InputStream input = read();
            try {
                return input.available();
            }
            catch(Exception ignored) {
            }
            finally {
                try {
                    input.close();
                }
                catch(IOException ignored) {
                }
            }
            return 0;
        }
        return file().length();
    }

    public long lastModified() {
        return file().lastModified();
    }

    public String toString() {
        return file.getPath();
    }

    static public CustomFileDescriptor tempFile(String prefix) {
        try {
            return new CustomFileDescriptor(File.createTempFile(prefix, null));
        }
        catch(IOException ex) {
            throw new RuntimeException("Unable to create temp file.", ex);
        }
    }

    static public CustomFileDescriptor tempDirectory(String prefix) {
        try {
            File file = File.createTempFile(prefix, null);
            if(!file.delete()) throw new IOException("Unable to delete temp file: " + file);
            if(!file.mkdir()) throw new IOException("Unable to create temp directory: " + file);
            return new CustomFileDescriptor(file);
        }
        catch(IOException ex) {
            throw new RuntimeException("Unable to create temp file.", ex);
        }
    }

    static private boolean deleteDirectory(File file) {
        if(file.exists()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(int i = 0, n = files.length; i < n; i++) {
                    if(files[i].isDirectory())
                        deleteDirectory(files[i]);
                    else
                        files[i].delete();
                }
            }
        }
        return file.delete();
    }

    static private void copyFile(CustomFileDescriptor source, CustomFileDescriptor dest) {
        try {
            dest.write(source.read(), false);
        }
        catch(Exception ex) {
            throw new RuntimeException("Error copying source file: " + source.file + " (" + source.type + ")\n" //
                    + "To destination: " + dest.file + " (" + dest.type + ")", ex);
        }
    }

    static private void copyDirectory(CustomFileDescriptor sourceDir, CustomFileDescriptor destDir) {
        destDir.mkdirs();
        CustomFileDescriptor[] files = sourceDir.list();
        for(int i = 0, n = files.length; i < n; i++) {
            CustomFileDescriptor srcFile = files[i];
            CustomFileDescriptor destFile = destDir.child(srcFile.name());
            if(srcFile.isDirectory())
                copyDirectory(srcFile, destFile);
            else
                copyFile(srcFile, destFile);
        }
    }
}