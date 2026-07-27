package com.github.xpenatan.jParser.builder.bundle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class NativeArchiveInspector {
    private static final byte[] ARCHIVE_MAGIC = "!<arch>\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] THIN_ARCHIVE_MAGIC = "!<thin>\n".getBytes(StandardCharsets.US_ASCII);

    private NativeArchiveInspector() {
    }

    static void requireStaticArchive(Path path, String description) throws IOException {
        requireRegularFile(path, description);
        try(RandomAccessFile input = new RandomAccessFile(path.toFile(), "r")) {
            byte[] magic = new byte[ARCHIVE_MAGIC.length];
            if(input.length() < magic.length) {
                throw new IllegalArgumentException(description + " is not an archive: " + path);
            }
            input.readFully(magic);
            if(matches(magic, THIN_ARCHIVE_MAGIC)) {
                throw new IllegalArgumentException(description + " must be self-contained, not a thin archive: " + path);
            }
            if(!matches(magic, ARCHIVE_MAGIC)) {
                throw new IllegalArgumentException(description + " is not an ar/COFF static archive: " + path);
            }

            long position = ARCHIVE_MAGIC.length;
            int payloadMembers = 0;
            boolean containsImportObject = false;
            while(position < input.length()) {
                if(input.length() - position < 60) {
                    throw new IllegalArgumentException(description + " has a truncated archive header: " + path);
                }
                input.seek(position);
                byte[] header = new byte[60];
                input.readFully(header);
                if(header[58] != '`' || header[59] != '\n') {
                    throw new IllegalArgumentException(description + " has an invalid archive member header: " + path);
                }
                long size = parseSize(header, path, description);
                long dataPosition = position + header.length;
                long nextPosition = dataPosition + size + (size & 1L);
                if(size < 0 || nextPosition < dataPosition || nextPosition > input.length()) {
                    throw new IllegalArgumentException(description + " has an invalid archive member size: " + path);
                }

                String memberName = new String(header, 0, 16, StandardCharsets.US_ASCII).trim();
                if(!isArchiveIndex(memberName)) {
                    payloadMembers++;
                    if(size >= 4) {
                        input.seek(dataPosition);
                        int first = input.readUnsignedByte();
                        int second = input.readUnsignedByte();
                        int third = input.readUnsignedByte();
                        int fourth = input.readUnsignedByte();
                        if(first == 0 && second == 0 && third == 0xff && fourth == 0xff) {
                            containsImportObject = true;
                        }
                    }
                }
                position = nextPosition;
            }
            if(payloadMembers == 0) {
                throw new IllegalArgumentException(description + " contains no object members: " + path);
            }
            if(containsImportObject) {
                throw new IllegalArgumentException(description + " is or contains a Windows import library: " + path);
            }
        }
    }

    static void requireRegularFile(Path path, String description) {
        if(path == null) {
            throw new IllegalArgumentException(description + " is required");
        }
        if(!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(description + " does not exist or is not a regular file: " + path);
        }
    }

    private static long parseSize(byte[] header, Path path, String description) {
        String value = new String(header, 48, 10, StandardCharsets.US_ASCII).trim();
        try {
            return Long.parseLong(value);
        }
        catch(NumberFormatException exception) {
            throw new IllegalArgumentException(description + " has a non-numeric archive member size: " + path,
                    exception);
        }
    }

    private static boolean isArchiveIndex(String memberName) {
        return memberName.equals("/")
                || memberName.equals("//")
                || memberName.startsWith("__.SYMDEF");
    }

    private static boolean matches(byte[] left, byte[] right) {
        if(left.length != right.length) {
            return false;
        }
        for(int i = 0; i < left.length; i++) {
            if(left[i] != right[i]) {
                return false;
            }
        }
        return true;
    }
}
