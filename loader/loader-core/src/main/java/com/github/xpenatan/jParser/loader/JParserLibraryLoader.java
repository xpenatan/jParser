package com.github.xpenatan.jParser.loader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.CRC32;

public class JParserLibraryLoader {

    static public Os os;
    static public Architecture.Bitness bitness = Architecture.Bitness._32;
    static public Architecture architecture = Architecture.x86;

    static {
        if (System.getProperty("os.name").contains("Windows"))
            os = Os.Windows;
        else if (System.getProperty("os.name").contains("Linux"))
            os = Os.Linux;
        else if (System.getProperty("os.name").contains("Mac"))
            os = Os.MacOsX;

        if (System.getProperty("os.arch").startsWith("arm") || System.getProperty("os.arch").startsWith("aarch64"))
            architecture = Architecture.ARM;
        else if (System.getProperty("os.arch").startsWith("riscv"))
            architecture = Architecture.RISCV;
        else if (System.getProperty("os.arch").startsWith("loongarch"))
            architecture = Architecture.LOONGARCH;

        if (System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8"))
            bitness = Architecture.Bitness._64;
        else if (System.getProperty("os.arch").contains("128"))
            bitness = Architecture.Bitness._128;

        boolean isMOEiOS = System.getProperty("moe.platform.name") != null;
        String vm = System.getProperty("java.runtime.name");
        if (vm != null && vm.contains("Android Runtime")) {
            os = Os.Android;
            bitness = Architecture.Bitness._32;
            architecture = Architecture.x86;
        }
        if (isMOEiOS || (os != Os.Android && os != Os.Windows && os != Os.Linux && os != Os.MacOsX)) {
            os = Os.IOS;
            bitness = Architecture.Bitness._32;
            architecture = Architecture.x86;
        }
    }

    private static final HashSet<String> loadedLibraries = new HashSet<>();

    private JParserLibraryLoader() {}

    /**
     * Synchronous loading is supported only on desktop and mobile targets; the web target uses asynchronous loading.
     */
    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, null, listener);
    }

    /**
     * Synchronous loading is supported only on desktop and mobile targets; the web target uses asynchronous loading.
     */
    public static void load(String libraryName, JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, options, listener);
    }

    private static void loadInternal(String libraryName, JParserLibraryLoaderOptions options, JParserLibraryLoaderListener listener) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }
        try {
            String path = null;
            if(options != null) {
                path = options.path;
            }

            String prefix = "";
            String suffix = "";
            if (os != Os.Android) {
                if(options == null || options.autoAddPrefix) {
                    prefix = os.getLibPrefix();
                }
                if(options == null || options.autoAddSuffix) {
                    suffix = architecture.toSuffix() + bitness.toSuffix();
                }
                suffix = suffix + "." + os.getLibExtension();
            }
            load(libraryName, path, prefix, suffix);
            listener.onLoad(true, null);
        }
        catch(Exception e) {
            listener.onLoad(false, e);
        }
    }

    private static void load(String libraryName, String path, String prefix, String suffix) {
        if (os == Os.IOS) return;
        if(path == null || os == Os.Android) {
            path = "";
        }
        else {
            path += "/";
            path = path.replace("//", "/");
        }

        final String fullLibraryName = path + libraryName;
        final String sourcePath = path + prefix + libraryName + suffix;

        if(loadedLibraries.contains(fullLibraryName)) {
            // Already loaed. Just ignore.
            return;
        }

        if (os == Os.Android)
            System.loadLibrary(sourcePath);
        else {
            InputStream input = readFile(sourcePath);
            String sourceCrc = crc(input);

            String fileName = new File(sourcePath).getName();

            // Temp directory with username in path.
            String tmpDir = System.getProperty("java.io.tmpdir") + "/jParser" + System.getProperty("user.name") + "/" + sourceCrc;
            File file = new File(tmpDir, fileName);
            Throwable ex = loadFile(sourcePath, sourceCrc, file);
            if (ex == null) return;

            // System provided temp directory.
            try {
                file = File.createTempFile(sourceCrc, null);
                if (file.delete() && loadFile(sourcePath, sourceCrc, file) == null) return;
            } catch (Throwable ignored) {
            }

            // User home.
            file = new File(System.getProperty("user.home") + "/.libgdx/" + sourceCrc, fileName);
            if (loadFile(sourcePath, sourceCrc, file) == null) return;

            // Relative directory.
            file = new File(".temp/" + sourceCrc, fileName);
            if (loadFile(sourcePath, sourceCrc, file) == null) return;

            // Fallback to java.library.path location, eg for applets.
            file = new File(System.getProperty("java.library.path"), sourcePath);
            if (file.exists()) {
                System.load(file.getAbsolutePath());
                return;
            }
        }

        loadedLibraries.add(fullLibraryName);
    }

    private static Throwable loadFile (String sourcePath, String sourceCrc, File extractedFile) {
        try {
            System.load(extractFile(sourcePath, sourceCrc, extractedFile).getAbsolutePath());
            return null;
        } catch (Throwable ex) {
            return ex;
        }
    }

    private static InputStream readFile (String path) {
        InputStream input = JParserLibraryLoader.class.getResourceAsStream("/" + path);
        if (input == null) throw new JParserSharedLibraryLoadRuntimeException("Unable to read file for extraction: " + path);
        return input;
    }

    private static String crc(InputStream input) {
        if (input == null) throw new IllegalArgumentException("input cannot be null.");
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[4096];
        try {
            while (true) {
                int length = input.read(buffer);
                if (length == -1) break;
                crc.update(buffer, 0, length);
            }
        } catch (Exception ex) {
        } finally {
            closeQuietly(input);
        }
        return Long.toString(crc.getValue(), 16);
    }

    private static File extractFile (String sourcePath, String sourceCrc, File extractedFile) throws IOException {
        String extractedCrc = null;
        if (extractedFile.exists()) {
            try {
                extractedCrc = crc(new FileInputStream(extractedFile));
            } catch (FileNotFoundException ignored) {
            }
        }

        // If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
        if (extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
            InputStream input = null;
            FileOutputStream output = null;
            try {
                input = readFile(sourcePath);
                extractedFile.getParentFile().mkdirs();
                output = new FileOutputStream(extractedFile);
                byte[] buffer = new byte[4096];
                while (true) {
                    int length = input.read(buffer);
                    if (length == -1) break;
                    output.write(buffer, 0, length);
                }
            } catch (IOException ex) {
                throw new JParserSharedLibraryLoadRuntimeException("Error extracting file: " + sourcePath + "\nTo: " + extractedFile.getAbsolutePath(),
                        ex);
            } finally {
                closeQuietly(input);
                closeQuietly(output);
            }
        }

        return extractedFile;
    }

    public static void closeQuietly (Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private enum Os {
        Windows, Linux, MacOsX, Android, IOS;

        public String getJniPlatform () {
            if (this == Os.Windows) return "win32";
            if (this == Os.Linux) return "linux";
            if (this == Os.MacOsX) return "mac";
            return "";
        }

        public String getLibPrefix () {
            if (this == Os.Linux || this == Os.Android || this == Os.MacOsX) {
                return "lib";
            }
            return "";
        }

        public String getLibExtension () {
            if (this == Os.Windows) return "dll";
            if (this == Os.Linux) return "so";
            if (this == Os.MacOsX) return "dylib";
            if (this == Os.Android) return "so";
            return "";
        }
    }

    private enum Architecture {
        x86, ARM, RISCV,LOONGARCH;

        public String toSuffix() {
            if (this == x86) return "";
            else return this.name().toLowerCase();
        }

        public enum Bitness {
            _32, _64, _128;

            public String toSuffix() {
                if (this == _32) return "";
                else return this.name().substring(1);
            }
        }
    }

    public static class JParserSharedLibraryLoadRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 8263101105331379889L;

        public JParserSharedLibraryLoadRuntimeException (String message) {
            super(message);
        }

        public JParserSharedLibraryLoadRuntimeException (Throwable t) {
            super(t);
        }

        public JParserSharedLibraryLoadRuntimeException (String message, Throwable t) {
            super(message, t);
        }
    }
}
