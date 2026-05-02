package com.github.xpenatan.jParser.builder;

import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class JProcess {

    public static void executeNdk (String directory, Map<String, String> environment) {
        CustomFileDescriptor build = new CustomFileDescriptor(directory);
        String command = "ndk-build";
        String [] array = new String[1];
        array[0] = command;
        startProcess(build.file(), array, environment);
    }

    public static boolean startProcess (ArrayList<String> command, Map<String, String> environment) {
        String [] array = new String[command.size()];
        command.toArray(array);
        return startProcess(new File(System.getProperty("user.home")), array, environment);
    }

    public static boolean startProcess (File directory, ArrayList<String> command,  Map<String, String> environment) {
        String [] array = new String[command.size()];
        command.toArray(array);
        return startProcess(directory, array, environment);
    }

    public static boolean startProcess (File directory, String[] commands, Map<String, String> environment) {
        try {
            System.out.println("Directory: " + directory.getPath());
            System.out.println("Command: " + commands[0]);
            for(int i = 1; i < commands.length; i++) {
                String command = commands[i];
                System.out.println("Param: " + command);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.environment().putAll(environment);
            final Process process = processBuilder
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
                    line = fixErrorPath(line, ": error");
                    line = fixErrorPath(line, ": warning ");
                    line = fixErrorPath(line, ": note: ");
                    System.err.println(line);
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

    private static String fixErrorPath(String line, String verification) {
        if(line.contains(verification)) {
            String[] lineSplit = line.split(verification);
            String leftSide = lineSplit[0];
            String rightSide = lineSplit[1];
            leftSide = leftSide.replace("\\", "/").replace("/", File.separator);
            String fixed = leftSide.replace("(", ":").replace(")", ":");
            line = fixed + verification + rightSide;
        }
        return line;
    }
}
