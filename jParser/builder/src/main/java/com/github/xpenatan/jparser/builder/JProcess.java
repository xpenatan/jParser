package com.github.xpenatan.jparser.builder;

import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JProcess {

    public static void executeNdk (String directory) {
        CustomFileDescriptor build = new CustomFileDescriptor(directory);
        String command = "ndk-build";
        String [] array = new String[1];
        array[0] = command;
        startProcess(build.file(), array);
    }

    public static boolean startProcess (ArrayList<String> command) {
        String [] array = new String[command.size()];
        command.toArray(array);
        return startProcess(new File(System.getProperty("user.home")), array);
    }

    public static boolean startProcess (File directory, ArrayList<String> command) {
        String [] array = new String[command.size()];
        command.toArray(array);
        return startProcess(directory, array);
    }

    public static boolean startProcess (File directory, String[] commands) {
        try {
            System.out.println("Directory: " + directory.getPath());
            System.out.println("Command: " + commands[0]);
            for(int i = 1; i < commands.length; i++) {
                String command = commands[i];
                System.out.println("Param: " + command);
            }

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
                    if(line.contains(": error")) {
                        // Make error clickable
                        String[] lineSplit = line.split(": error");
                        String leftSide = lineSplit[0];
                        String rightSide = lineSplit[1];
                        String fixed = leftSide.replace("(", ":").replace(")", ":");
                        line = fixed + " error" + rightSide;
                    }
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
}
