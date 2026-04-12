package cn.creekmoon.excel2markdown;

/**
 * Markdown 输出策略，控制是否在表格中附加行号和列号。
 */
public enum MarkdownOutputStrategy {

    /**
     * 所见即所得（默认）。不添加列字母表头行和行号首列，
     * 数据第一行直接作为 Markdown 表格标题行输出。
     */
    WYSIWYG,

    /**
     * 原生坐标模式。每张表格前输出一行英文说明，
     * 表格标题行使用 Excel 列字母（A、B、C…），
     * 每行数据前追加从 1 开始的行号，左上角占位标签为 {@code Rows}。
     */
    NATIVE_COORDINATES
}
