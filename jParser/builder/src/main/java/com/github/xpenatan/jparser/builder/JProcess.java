package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JProcess {

    public static void executeNdk (String directory) {
        CustomFileDescriptor build = new CustomFileDescriptor(directory);
        String command = "ndk-build";
        startProcess(build.file(), command);
    }

    public static boolean startProcess (String command) {
        return startProcess(new File(System.getProperty("user.home")), command);
    }

    public static boolean startProcess (File directory, String command) {
        String[] commands = command.replaceAll("\\s+", " ").split(" ");
        return startProcess(directory, commands);
    }

    public static boolean startProcess (File directory, String... commands) {
        try {
            System.out.println("Command: " + commands[0]);
            for(int i = 1; i < commands.length; i++) {
                String command = commands[i];
                System.out.println("Param: " + command);
            }
            System.out.println();

            final Process process = new ProcessBuilder(commands)
                    .redirectErrorStream(true)
                    .directory(directory)
                    .start();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run () {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            // augment output with java file line references :D
                            printFileLineNumber(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private void printFileLineNumber (String line) {
                    System.out.println(line);
                }
            });
            t.setDaemon(true);
            t.start();
            process.waitFor();
            t.join();
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
