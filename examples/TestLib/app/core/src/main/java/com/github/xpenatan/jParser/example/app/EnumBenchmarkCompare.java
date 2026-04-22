package com.github.xpenatan.jParser.example.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads two enum benchmark CSV files (JNI and FFM) and prints a side-by-side
 * comparison table to stdout, and optionally writes it to a file.
 * <p>
 * Usage: {@code java EnumBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]}
 */
public class EnumBenchmarkCompare {

    private static class Row {
        final String label;
        final double medianMs;
        final double mopsPerSec;

        Row(String label, double medianMs, double mopsPerSec) {
            this.label = label;
            this.medianMs = medianMs;
            this.mopsPerSec = mopsPerSec;
        }
    }

    private static class CsvData {
        final Map<String, Row> rows = new LinkedHashMap<>();
        String warmup = "?";
        String timed = "?";
        String iterations = "?";
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage: EnumBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]");
            System.exit(1);
        }

        CsvData jniData = readCsv(args[0]);
        CsvData ffmData = readCsv(args[1]);

        if(jniData.rows.isEmpty() || ffmData.rows.isEmpty()) {
            System.err.println("ERROR: One or both CSV files are empty or could not be read.");
            System.exit(1);
        }

        String outputPath = args.length >= 3 ? args[2] : null;
        printComparison(jniData, ffmData, outputPath);
    }

    private static CsvData readCsv(String path) {
        CsvData data = new CsvData();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) continue;
                if(line.startsWith("#")) {
                    String meta = line.substring(1).trim();
                    if(meta.startsWith("warmup=")) data.warmup = meta.substring(7);
                    else if(meta.startsWith("timed=")) data.timed = meta.substring(6);
                    else if(meta.startsWith("iterations=")) data.iterations = meta.substring(11);
                    continue;
                }
                if(line.startsWith("label,")) continue;

                int lastComma = line.lastIndexOf(',');
                int secondLastComma = line.lastIndexOf(',', lastComma - 1);
                if(secondLastComma < 0) continue;

                String label = line.substring(0, secondLastComma);
                double medianMs = Double.parseDouble(line.substring(secondLastComma + 1, lastComma));
                double mopsPerSec = Double.parseDouble(line.substring(lastComma + 1));
                data.rows.put(label, new Row(label, medianMs, mopsPerSec));
            }
        } catch(IOException e) {
            System.err.println("Failed to read CSV: " + path + " -- " + e.getMessage());
        }
        return data;
    }

    private static void printComparison(CsvData jniData, CsvData ffmData, String outputPath) {
        Map<String, Row> jniRows = jniData.rows;
        Map<String, Row> ffmRows = ffmData.rows;

        ArrayList<String> labels = new ArrayList<>(jniRows.keySet());
        for(String label : ffmRows.keySet()) {
            if(!labels.contains(label)) labels.add(label);
        }

        String sep = "+-" + pad(28) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(9) + "-+-"
                + pad(8) + "-+";

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("===================================================================\n");
        sb.append("  JNI vs FFM -- Enum Benchmark Comparison\n");
        sb.append("===================================================================\n");
        sb.append("  Warm-up rounds : ").append(jniData.warmup).append("\n");
        sb.append("  Timed rounds   : ").append(jniData.timed).append("\n");
        sb.append("  Iterations     : ").append(jniData.iterations).append("\n");
        sb.append("===================================================================\n");
        sb.append("\n");
        sb.append(sep).append("\n");
        sb.append(String.format("| %-28s | %10s | %10s | %10s | %10s | %9s | %-8s |%n",
                "Benchmark", "JNI (ms)", "JNI Mops", "FFM (ms)", "FFM Mops", "Speedup", "Winner"));
        sb.append(sep).append("\n");

        int jniWins = 0;
        int ffmWins = 0;
        int ties = 0;

        for(String label : labels) {
            Row jni = jniRows.get(label);
            Row ffm = ffmRows.get(label);

            if(jni == null || ffm == null) {
                sb.append(String.format("| %-28s | %10s | %10s | %10s | %10s | %9s | %-8s |%n",
                        label,
                        jni != null ? String.format("%.1f", jni.medianMs) : "N/A",
                        jni != null ? String.format("%.2f", jni.mopsPerSec) : "N/A",
                        ffm != null ? String.format("%.1f", ffm.medianMs) : "N/A",
                        ffm != null ? String.format("%.2f", ffm.mopsPerSec) : "N/A",
                        "--", "--"));
                continue;
            }

            double speedup;
            String winner;
            if(jni.medianMs < ffm.medianMs) {
                speedup = ffm.medianMs / jni.medianMs;
                winner = "JNI";
                jniWins++;
            } else if(ffm.medianMs < jni.medianMs) {
                speedup = jni.medianMs / ffm.medianMs;
                winner = "FFM";
                ffmWins++;
            } else {
                speedup = 1.0;
                winner = "TIE";
                ties++;
            }

            sb.append(String.format("| %-28s | %10.1f | %10.2f | %10.1f | %10.2f | %8.2fx | %-8s |%n",
                    label, jni.medianMs, jni.mopsPerSec,
                    ffm.medianMs, ffm.mopsPerSec, speedup, winner));
        }

        sb.append(sep).append("\n");
        sb.append("\n");
        sb.append(String.format("  Summary: JNI wins %d, FFM wins %d, Ties %d (out of %d benchmarks)%n",
                jniWins, ffmWins, ties, labels.size()));
        sb.append("\n");

        String table = sb.toString();
        System.out.print(table);

        if(outputPath != null && !outputPath.isEmpty()) {
            try(PrintWriter pw = new PrintWriter(new FileWriter(outputPath))) {
                pw.print(table);
                System.out.println("  Comparison written to: " + outputPath);
            } catch(IOException e) {
                System.err.println("  Failed to write comparison file: " + e.getMessage());
            }
        }
    }

    private static String pad(int width) {
        StringBuilder sb = new StringBuilder();
        while(sb.length() < width) sb.append('-');
        return sb.toString();
    }
}

