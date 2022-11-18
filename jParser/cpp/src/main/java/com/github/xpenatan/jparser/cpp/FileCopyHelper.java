package com.github.xpenatan.jparser.cpp;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class FileCopyHelper {

    public static void copyDir(Path src, Path dest, String... excludes) throws IOException {
        File directory = dest.toFile();
        if(!directory.exists())
            directory.mkdirs();

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
                        String name = file.getName();
                        Path newDest = dest.resolve(name);
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
    }
}
