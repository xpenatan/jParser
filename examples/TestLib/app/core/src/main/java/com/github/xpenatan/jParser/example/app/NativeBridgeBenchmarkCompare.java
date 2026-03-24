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
 * Reads two benchmark CSV files (JNI and FFM) and prints a side-by-side
 * comparison table to stdout, and optionally writes it to a file.
 * <p>
 * Usage: {@code java NativeBridgeBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]}
 */
public class NativeBridgeBenchmarkCompare {

    private static class Row {
        final String label;
        final double medianMs;
        final double mcallsPerSec;

        Row(String label, double medianMs, double mcallsPerSec) {
            this.label = label;
            this.medianMs = medianMs;
            this.mcallsPerSec = mcallsPerSec;
        }
    }

    /** Holds parsed CSV rows together with optional metadata from comment lines. */
    private static class CsvData {
        final Map<String, Row> rows = new LinkedHashMap<>();
        String warmup = "?";
        String timed = "?";
        String calls = "?";
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage: NativeBridgeBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]");
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
                // Parse metadata comments (e.g. "# warmup=3")
                if(line.startsWith("#")) {
                    String meta = line.substring(1).trim();
                    if(meta.startsWith("warmup=")) data.warmup = meta.substring(7);
                    else if(meta.startsWith("timed=")) data.timed = meta.substring(6);
                    else if(meta.startsWith("calls=")) data.calls = meta.substring(6);
                    continue;
                }
                // Skip CSV header
                if(line.startsWith("label,")) continue;
                // Format: label,medianMs,mcallsPerSec
                int lastComma = line.lastIndexOf(',');
                int secondLastComma = line.lastIndexOf(',', lastComma - 1);
                if(secondLastComma < 0) continue;
                String label = line.substring(0, secondLastComma);
                double medianMs = Double.parseDouble(line.substring(secondLastComma + 1, lastComma));
                double mcalls = Double.parseDouble(line.substring(lastComma + 1));
                data.rows.put(label, new Row(label, medianMs, mcalls));
            }
        } catch(IOException e) {
            System.err.println("Failed to read CSV: " + path + " — " + e.getMessage());
        }
        return data;
    }

    private static void printComparison(CsvData jniData, CsvData ffmData, String outputPath) {
        Map<String, Row> jniRows = jniData.rows;
        Map<String, Row> ffmRows = ffmData.rows;

        // Collect all labels preserving order from JNI file
        ArrayList<String> labels = new ArrayList<>(jniRows.keySet());
        for(String l : ffmRows.keySet()) {
            if(!labels.contains(l)) labels.add(l);
        }

        String sep = "+-" + pad("", 34, '-') + "-+-"
                + pad("", 10, '-') + "-+-"
                + pad("", 12, '-') + "-+-"
                + pad("", 10, '-') + "-+-"
                + pad("", 12, '-') + "-+-"
                + pad("", 9, '-') + "-+-"
                + pad("", 8, '-') + "-+";

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=======================================================================\n");
        sb.append("  JNI vs FFM -- Benchmark Comparison\n");
        sb.append("=======================================================================\n");
        sb.append("  Warm-up rounds : ").append(jniData.warmup).append("\n");
        sb.append("  Timed rounds   : ").append(jniData.timed).append("\n");
        sb.append("  Calls/round    : ").append(jniData.calls).append("\n");
        sb.append("=======================================================================\n");
        sb.append("\n");
        sb.append(sep).append("\n");
        sb.append(String.format("| %-34s | %10s | %12s | %10s | %12s | %9s | %-8s |%n",
                "Benchmark", "JNI (ms)", "JNI Mcalls/s", "FFM (ms)", "FFM Mcalls/s", "Speedup", "Winner"));
        sb.append(sep).append("\n");

        int jniWins = 0;
        int ffmWins = 0;
        int ties = 0;

        for(String label : labels) {
            Row jni = jniRows.get(label);
            Row ffm = ffmRows.get(label);

            if(jni == null || ffm == null) {
                sb.append(String.format("| %-34s | %10s | %12s | %10s | %12s | %9s | %-8s |%n",
                        label,
                        jni != null ? String.format("%.1f", jni.medianMs) : "N/A",
                        jni != null ? String.format("%.2f", jni.mcallsPerSec) : "N/A",
                        ffm != null ? String.format("%.1f", ffm.medianMs) : "N/A",
                        ffm != null ? String.format("%.2f", ffm.mcallsPerSec) : "N/A",
                        "--", "--"));
                continue;
            }

            // Speedup: how much faster is the winner (ratio of slower/faster)
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

            sb.append(String.format("| %-34s | %10.1f | %12.2f | %10.1f | %12.2f | %8.2fx | %-8s |%n",
                    label, jni.medianMs, jni.mcallsPerSec,
                    ffm.medianMs, ffm.mcallsPerSec, speedup, winner));
        }

        sb.append(sep).append("\n");
        sb.append("\n");
        sb.append(String.format("  Summary: JNI wins %d, FFM wins %d, Ties %d (out of %d benchmarks)%n",
                jniWins, ffmWins, ties, labels.size()));
        sb.append("\n");

        // Print to stdout
        String table = sb.toString();
        System.out.print(table);

        // Write to file if output path was provided
        if(outputPath != null && !outputPath.isEmpty()) {
            try(PrintWriter pw = new PrintWriter(new FileWriter(outputPath))) {
                pw.print(table);
                System.out.println("  Comparison written to: " + outputPath);
            } catch(IOException e) {
                System.err.println("  Failed to write comparison file: " + e.getMessage());
            }
        }
    }

    private static String pad(String s, int width, char c) {
        StringBuilder sb = new StringBuilder(s);
        while(sb.length() < width) sb.append(c);
        return sb.toString();
    }
}

