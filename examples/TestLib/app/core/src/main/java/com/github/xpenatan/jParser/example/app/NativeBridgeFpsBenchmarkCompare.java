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
 * Reads two FPS benchmark CSV files (JNI and FFM) and prints a side-by-side
 * comparison table to stdout, and optionally writes it to a file.
 * <p>
 * Usage: {@code java NativeBridgeFpsBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]}
 */
public class NativeBridgeFpsBenchmarkCompare {

    private static class Row {
        final String label;
        final double avgFps;
        final double minFps;

        Row(String label, double avgFps, double minFps) {
            this.label = label;
            this.avgFps = avgFps;
            this.minFps = minFps;
        }
    }

    private static class CsvData {
        final Map<String, Row> rows = new LinkedHashMap<>();
        String callsPerFrame = "?";
        String warmupSec = "?";
        String measureSec = "?";
    }

    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Usage: NativeBridgeFpsBenchmarkCompare <jni.csv> <ffm.csv> [output.txt]");
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
                    if(meta.startsWith("calls_per_frame=")) data.callsPerFrame = meta.substring(16);
                    else if(meta.startsWith("warmup_sec=")) data.warmupSec = meta.substring(11);
                    else if(meta.startsWith("measure_sec=")) data.measureSec = meta.substring(12);
                    continue;
                }
                if(line.startsWith("label,")) continue;
                // Format: label,avgFps,minFps
                int lastComma = line.lastIndexOf(',');
                int secondLastComma = line.lastIndexOf(',', lastComma - 1);
                if(secondLastComma < 0) continue;
                String label = line.substring(0, secondLastComma);
                double avgFps = Double.parseDouble(line.substring(secondLastComma + 1, lastComma));
                double minFps = Double.parseDouble(line.substring(lastComma + 1));
                data.rows.put(label, new Row(label, avgFps, minFps));
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
        for(String l : ffmRows.keySet()) {
            if(!labels.contains(l)) labels.add(l);
        }

        String sep = "+-" + pad(34) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(10) + "-+-"
                + pad(9) + "-+-"
                + pad(8) + "-+";

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=======================================================================\n");
        sb.append("  JNI vs FFM -- FPS Benchmark Comparison\n");
        sb.append("=======================================================================\n");
        sb.append("  Calls/frame    : ").append(jniData.callsPerFrame).append("\n");
        sb.append("  Warm-up (sec)  : ").append(jniData.warmupSec).append("\n");
        sb.append("  Measure (sec)  : ").append(jniData.measureSec).append("\n");
        sb.append("=======================================================================\n");
        sb.append("\n");
        sb.append(sep).append("\n");
        sb.append(String.format("| %-34s | %10s | %10s | %10s | %10s | %9s | %-8s |%n",
                "Benchmark", "JNI avg", "JNI min", "FFM avg", "FFM min", "FPS gain", "Winner"));
        sb.append(sep).append("\n");

        int jniWins = 0;
        int ffmWins = 0;
        int ties = 0;

        for(String label : labels) {
            Row jni = jniRows.get(label);
            Row ffm = ffmRows.get(label);

            if(jni == null || ffm == null) {
                sb.append(String.format("| %-34s | %10s | %10s | %10s | %10s | %9s | %-8s |%n",
                        label,
                        jni != null ? String.format("%.1f", jni.avgFps) : "N/A",
                        jni != null ? String.format("%.1f", jni.minFps) : "N/A",
                        ffm != null ? String.format("%.1f", ffm.avgFps) : "N/A",
                        ffm != null ? String.format("%.1f", ffm.minFps) : "N/A",
                        "--", "--"));
                continue;
            }

            double gain;
            String winner;
            if(ffm.avgFps > jni.avgFps) {
                gain = ffm.avgFps - jni.avgFps;
                winner = "FFM";
                ffmWins++;
            } else if(jni.avgFps > ffm.avgFps) {
                gain = jni.avgFps - ffm.avgFps;
                winner = "JNI";
                jniWins++;
            } else {
                gain = 0;
                winner = "TIE";
                ties++;
            }

            sb.append(String.format("| %-34s | %10.1f | %10.1f | %10.1f | %10.1f | %+8.1f | %-8s |%n",
                    label, jni.avgFps, jni.minFps,
                    ffm.avgFps, ffm.minFps, winner.equals("FFM") ? gain : -gain, winner));
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

