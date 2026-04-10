package cn.creekmoon.excel2markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MarkdownRenderer {

    private final StringBuilder buffer = new StringBuilder();
    private final List<List<String>> currentTableRows = new ArrayList<>();

    void beginSheet(String sheetName) {
        flushTable();
        if (buffer.length() > 0) {
            buffer.append("\n");
        }
        buffer.append("# ").append(sheetName).append("\n\n");
    }

    void addRow(List<String> cells) {
        List<String> normalizedRow = normalizeRow(cells);
        if (normalizedRow.isEmpty()) {
            flushTable();
            appendBlankLine();
            return;
        }

        /* 单行文本块单独输出，多列表格块连续收集 */
        if (countNonBlankCells(normalizedRow) == 1) {
            flushTable();
            buffer.append(escapeCell(extractSingleCellValue(normalizedRow))).append("\n\n");
            return;
        }
        currentTableRows.add(normalizedRow);
    }

    String render() {
        flushTable();
        return buffer.toString().trim();
    }

    private void flushTable() {
        if (currentTableRows.isEmpty()) {
            return;
        }
        int columnCount = currentTableRows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
        if (columnCount == 0) {
            currentTableRows.clear();
            return;
        }

        buffer.append(buildRow(padRow(currentTableRows.get(0), columnCount))).append("\n");
        buffer.append(buildSeparatorRow(columnCount)).append("\n");
        for (int i = 1; i < currentTableRows.size(); i++) {
            buffer.append(buildRow(padRow(currentTableRows.get(i), columnCount))).append("\n");
        }
        buffer.append("\n");
        currentTableRows.clear();
    }

    private List<String> normalizeRow(List<String> cells) {
        if (cells == null || cells.isEmpty()) {
            return Collections.emptyList();
        }
        int lastNonBlankIndex = -1;
        for (int i = cells.size() - 1; i >= 0; i--) {
            if (!isBlank(cells.get(i))) {
                lastNonBlankIndex = i;
                break;
            }
        }
        if (lastNonBlankIndex < 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(cells.subList(0, lastNonBlankIndex + 1));
    }

    private List<String> padRow(List<String> cells, int columnCount) {
        List<String> paddedRow = new ArrayList<>(cells);
        while (paddedRow.size() < columnCount) {
            paddedRow.add("");
        }
        return paddedRow;
    }

    private int countNonBlankCells(List<String> cells) {
        int count = 0;
        for (String cell : cells) {
            if (!isBlank(cell)) {
                count++;
            }
        }
        return count;
    }

    private String extractSingleCellValue(List<String> cells) {
        for (String cell : cells) {
            if (!isBlank(cell)) {
                return cell;
            }
        }
        return "";
    }

    private void appendBlankLine() {
        if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) == '\n') {
            return;
        }
        buffer.append("\n\n");
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
        /* 转义管道符，单元格内换行替换为 <br> */
        return value.replace("|", "\\|").replace("\r\n", "<br>").replace("\n", "<br>").replace("\r", "<br>");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
