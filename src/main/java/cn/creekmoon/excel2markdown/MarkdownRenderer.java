package cn.creekmoon.excel2markdown;

import java.util.ArrayList;
import java.util.List;

class MarkdownRenderer {

    private final StringBuilder buffer = new StringBuilder();
    private final List<List<String>> currentSheetRows = new ArrayList<>();
    private boolean sheetStarted = false;

    void beginSheet(String sheetName) {
        flushSheet();
        if (buffer.length() > 0) {
            buffer.append("\n");
        }
        buffer.append("# ").append(sheetName).append("\n\n");
        sheetStarted = true;
    }

    void addRow(List<String> cells) {
        currentSheetRows.add(cells == null ? List.of() : new ArrayList<>(cells));
    }

    String render() {
        flushSheet();
        return buffer.toString().trim();
    }

    private void flushSheet() {
        if (!sheetStarted && currentSheetRows.isEmpty()) {
            return;
        }

        int columnCount = currentSheetRows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        if (columnCount == 0) {
            currentSheetRows.clear();
            sheetStarted = false;
            return;
        }

        /* header row: A, B, C, ..., Z, AA, AB, ... */
        List<String> headerCells = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            headerCells.add(columnLabel(i));
        }
        buffer.append(buildRow(headerCells)).append("\n");
        buffer.append(buildSeparatorRow(columnCount)).append("\n");

        for (List<String> row : currentSheetRows) {
            buffer.append(buildRow(padRow(row, columnCount))).append("\n");
        }
        buffer.append("\n");

        currentSheetRows.clear();
        sheetStarted = false;
    }

    private List<String> padRow(List<String> cells, int columnCount) {
        if (cells.size() == columnCount) {
            return cells;
        }
        List<String> padded = new ArrayList<>(cells);
        while (padded.size() < columnCount) {
            padded.add("");
        }
        return padded;
    }

    private String buildRow(List<String> cells) {
        StringBuilder row = new StringBuilder("|");
        for (String cell : cells) {
            row.append(" ").append(escapeCell(cell)).append(" |");
        }
        return row.toString();
    }

    private String buildSeparatorRow(int columnCount) {
        StringBuilder sep = new StringBuilder("|");
        for (int i = 0; i < columnCount; i++) {
            sep.append(" --- |");
        }
        return sep.toString();
    }

    private String escapeCell(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace("|", "\\|")
                .replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .replace("\r", "<br>");
    }

    /**
     * Converts a 0-based column index to an Excel-style column label.
     * 0 -> A, 25 -> Z, 26 -> AA, 27 -> AB, ...
     */
    static String columnLabel(int index) {
        StringBuilder label = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            n--;
            label.insert(0, (char) ('A' + n % 26));
            n /= 26;
        }
        return label.toString();
    }
}
