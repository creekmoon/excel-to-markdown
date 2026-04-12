package cn.creekmoon.excel2markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 将解析好的行数据渲染为 Markdown 表格字符串。
 * 支持 {@link MarkdownOutputStrategy#WYSIWYG} 和
 * {@link MarkdownOutputStrategy#NATIVE_COORDINATES} 两种输出策略。
 */
class MarkdownRenderer {

    private final MarkdownOutputStrategy strategy;
    private final StringBuilder buffer = new StringBuilder();
    private final List<List<String>> currentSheetRows = new ArrayList<>();
    private boolean sheetStarted = false;

    /** 使用指定策略构造渲染器。 */
    MarkdownRenderer(MarkdownOutputStrategy strategy) {
        this.strategy = strategy;
    }

    /** 开始一个新的 Sheet，会先把上一个 Sheet 的行数据刷入缓冲区。 */
    void beginSheet(String sheetName) {
        flushSheet();
        if (buffer.length() > 0) {
            buffer.append("\n");
        }
        buffer.append("# ").append(sheetName).append("\n\n");
        sheetStarted = true;
    }

    /** 追加一行数据。 */
    void addRow(List<String> cells) {
        currentSheetRows.add(cells == null ? Collections.emptyList() : new ArrayList<>(cells));
    }

    /** 将所有缓冲数据渲染为最终 Markdown 字符串并返回。 */
    String render() {
        flushSheet();
        return buffer.toString().trim();
    }

    private void flushSheet() {
        if (!sheetStarted && currentSheetRows.isEmpty()) {
            return;
        }

        /* 计算最大列数 */
        int columnCount = currentSheetRows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        if (columnCount == 0) {
            currentSheetRows.clear();
            sheetStarted = false;
            return;
        }

        if (strategy == MarkdownOutputStrategy.NATIVE_COORDINATES) {
            flushSheetWithCoordinates(columnCount);
        } else {
            flushSheetWysiwyg(columnCount);
        }

        currentSheetRows.clear();
        sheetStarted = false;
    }

    /* 所见即所得模式：首行数据作为表格标题行，不附加行号/列号 */
    private void flushSheetWysiwyg(int columnCount) {
        /* 首行作表头，后续行作数据 */
        List<String> header = currentSheetRows.isEmpty()
                ? Collections.emptyList()
                : padRow(currentSheetRows.get(0), columnCount);
        buffer.append(buildRow(header)).append("\n");
        buffer.append(buildSeparatorRow(columnCount)).append("\n");
        for (int i = 1; i < currentSheetRows.size(); i++) {
            buffer.append(buildRow(padRow(currentSheetRows.get(i), columnCount))).append("\n");
        }
        buffer.append("\n");
    }

    /* 原生坐标模式：前置英文说明，列字母表头，每行前缀行号 */
    private void flushSheetWithCoordinates(int columnCount) {
        buffer.append("> Note: Column headers use Excel column letters (A, B, C ...).")
              .append(" Row numbers reflect the native row index starting from 1.\n\n");

        /* 表头行：左上角 Rows，后接 A B C ... */
        List<String> headerCells = new ArrayList<>(columnCount + 1);
        headerCells.add("Rows");
        for (int i = 0; i < columnCount; i++) {
            headerCells.add(columnLabel(i));
        }
        buffer.append(buildRow(headerCells)).append("\n");
        buffer.append(buildSeparatorRow(columnCount + 1)).append("\n");

        /* 数据行：首列追加 1-based 行号 */
        for (int rowIdx = 0; rowIdx < currentSheetRows.size(); rowIdx++) {
            List<String> paddedData = padRow(currentSheetRows.get(rowIdx), columnCount);
            List<String> rowWithNum = new ArrayList<>(columnCount + 1);
            rowWithNum.add(String.valueOf(rowIdx + 1));
            rowWithNum.addAll(paddedData);
            buffer.append(buildRow(rowWithNum)).append("\n");
        }
        buffer.append("\n");
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
     * 将 0-based 列索引转换为 Excel 列字母标签。
     * 例如：0 → A，25 → Z，26 → AA，27 → AB。
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
