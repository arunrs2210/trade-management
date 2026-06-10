package com.shnoor.util;

import java.util.List;

/**
 * Simple console table printer.
 * Demonstrates array manipulation and string formatting.
 */
public class TablePrinter {

    /**
     * Prints a formatted table to stdout.
     *
     * @param headers Column headers
     * @param rows    Each inner list is one row; values must align with headers
     */
    public static void print(List<String> headers, List<List<String>> rows) {
        // Calculate column widths (max of header and each cell)
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(row.size(), widths.length); i++) {
                widths[i] = Math.max(widths[i], row.get(i) == null ? 4 : row.get(i).length());
            }
        }

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildRow(headers, widths));
        System.out.println(separator);
        for (List<String> row : rows) {
            System.out.println(buildRow(row, widths));
        }
        System.out.println(separator);
        System.out.printf("  %d row(s) returned.%n%n", rows.size());
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        return sb.toString();
    }

    private static String buildRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.size() && cells.get(i) != null) ? cells.get(i) : "NULL";
            sb.append(String.format(" %-" + widths[i] + "s |", cell));
        }
        return sb.toString();
    }
}
