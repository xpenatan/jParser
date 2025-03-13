package com.github.xpenatan.jparser.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileHelper {

    public static ArrayList<String> copyDir(String src, String dest, String... excludes) throws IOException {
        Path srcPath = new File(src).toPath();
        Path destPath = new File(dest).toPath();
        return copyDir(srcPath, destPath, excludes);
    }

    public static ArrayList<String> copyDir(Path src, Path dest, String... excludes) throws IOException {
        ArrayList<String> outPath = new ArrayList<>();
        File directory = dest.toFile();
        if(!directory.exists())
            directory.mkdirs();

        String srcFullPath = src.toFile().getCanonicalPath();

        if(Files.exists(src)) {
            Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    boolean skip = false;
                    if(excludes != null) {
                        String fileStr = path.getFileName().toString();
                        for(int i = 0; i < excludes.length; i++) {
                            String excludeFile = excludes[i];
                            if(fileStr.contains(excludeFile)) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if(!skip) {
                        File file = path.toFile();
                        String fullPath = file.getCanonicalPath();
                        String name = fullPath.replace(srcFullPath, "");
                        File file1 = new File(dest + name);
                        File parentFile = file1.getParentFile();
                        if(!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        Path newDest = file1.toPath();
                        String canonicalPath = file1.getCanonicalPath();
                        outPath.add(canonicalPath);
                        Files.copy(path, newDest, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return outPath;
    }

    public static ArrayList<String> getFilesFromDir(String src) {
        Path srcPath = new File(src).toPath();
        try {
            return copyDir(srcPath);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> copyDir(Path src) throws IOException {
        ArrayList<String> outPath = new ArrayList<>();
        String srcFullPath = src.toFile().getCanonicalPath().replace("\\", "/");
        boolean exists = Files.exists(src);
        System.out.println("CopyDir: ");
        System.out.println(exists + " " + srcFullPath);
        if(exists) {
            Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    File file = path.toFile();
                    String fullPath = file.getCanonicalPath();
                    String name = fullPath.replace(srcFullPath, "");
                    outPath.add(file.getCanonicalPath());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return outPath;
    }
}
