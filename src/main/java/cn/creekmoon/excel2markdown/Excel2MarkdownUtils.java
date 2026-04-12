package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel / CSV 转 Markdown 的工具类入口，所有方法均为静态方法。
 *
 * <p>默认使用 {@link MarkdownOutputStrategy#WYSIWYG} 策略（所见即所得），
 * 如需输出原生坐标（列字母 + 行号），可调用带 {@link MarkdownOutputStrategy} 参数的重载方法。
 */
public class Excel2MarkdownUtils {

    private Excel2MarkdownUtils() {
    }

    /* ── xlsx → String ─────────────────────────────────────────────── */

    /**
     * 将 xlsx 文件转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String xlsx2Markdown(File file) throws Excel2MarkdownException {
        return xlsx2Markdown(file, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xlsx 文件按指定策略转为 Markdown 字符串。
     */
    public static String xlsx2Markdown(File file, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return xlsx2Markdown(is, strategy);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将 xlsx 输入流转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String xlsx2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        return xlsx2Markdown(inputStream, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xlsx 输入流按指定策略转为 Markdown 字符串。
     */
    public static String xlsx2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        /* 解析并渲染 */
        MarkdownRenderer renderer = new MarkdownRenderer(strategy);
        ConvertConfig config = ConvertConfig.defaults();
        XlsxSaxParser parser = new XlsxSaxParser();
        parser.parse(inputStream, config, (sheetName, rows) -> {
            renderer.beginSheet(sheetName);
            for (List<String> row : rows) {
                renderer.addRow(row);
            }
        });
        return renderer.render();
    }

    /* ── xls → String ──────────────────────────────────────────────── */

    /**
     * 将 xls 文件转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String xls2Markdown(File file) throws Excel2MarkdownException {
        return xls2Markdown(file, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xls 文件按指定策略转为 Markdown 字符串。
     */
    public static String xls2Markdown(File file, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return xls2Markdown(is, strategy);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将 xls 输入流转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String xls2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        return xls2Markdown(inputStream, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xls 输入流按指定策略转为 Markdown 字符串。
     */
    public static String xls2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        /* 解析并渲染 */
        MarkdownRenderer renderer = new MarkdownRenderer(strategy);
        ConvertConfig config = ConvertConfig.defaults();
        XlsParser parser = new XlsParser();
        parser.parse(inputStream, config, (sheetName, rows) -> {
            renderer.beginSheet(sheetName);
            for (List<String> row : rows) {
                renderer.addRow(row);
            }
        });
        return renderer.render();
    }

    /* ── xls → File ────────────────────────────────────────────────── */

    /**
     * 将 xls 文件转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void xls2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        xls2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xls 文件按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void xls2MarkdownFile(File source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(xls2Markdown(source, strategy), target);
    }

    /**
     * 将 xls 输入流转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void xls2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        xls2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xls 输入流按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void xls2MarkdownFile(InputStream source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(xls2Markdown(source, strategy), target);
    }

    /* ── csv → String ──────────────────────────────────────────────── */

    /**
     * 将 csv 文件转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String csv2Markdown(File file) throws Excel2MarkdownException {
        return csv2Markdown(file, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 csv 文件按指定策略转为 Markdown 字符串。
     */
    public static String csv2Markdown(File file, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return csv2Markdown(is, strategy);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将 csv 输入流转为 Markdown 字符串，使用所见即所得策略输出。
     */
    public static String csv2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        return csv2Markdown(inputStream, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 csv 输入流按指定策略转为 Markdown 字符串。
     */
    public static String csv2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        /* 解析并渲染 */
        MarkdownRenderer renderer = new MarkdownRenderer(strategy);
        ConvertConfig config = ConvertConfig.defaults();
        CsvLineParser parser = new CsvLineParser();
        parser.parse(inputStream, config.encoding, renderer::addRow);
        return renderer.render();
    }

    /* ── xlsx → File ───────────────────────────────────────────────── */

    /**
     * 将 xlsx 文件转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void xlsx2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        xlsx2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xlsx 文件按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void xlsx2MarkdownFile(File source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(xlsx2Markdown(source, strategy), target);
    }

    /**
     * 将 xlsx 输入流转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void xlsx2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        xlsx2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 xlsx 输入流按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void xlsx2MarkdownFile(InputStream source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(xlsx2Markdown(source, strategy), target);
    }

    /* ── csv → File ────────────────────────────────────────────────── */

    /**
     * 将 csv 文件转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void csv2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        csv2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 csv 文件按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void csv2MarkdownFile(File source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(csv2Markdown(source, strategy), target);
    }

    /**
     * 将 csv 输入流转为 Markdown 并写出到目标文件，使用所见即所得策略输出。
     */
    public static void csv2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        csv2MarkdownFile(source, target, MarkdownOutputStrategy.WYSIWYG);
    }

    /**
     * 将 csv 输入流按指定策略转为 Markdown 并写出到目标文件。
     */
    public static void csv2MarkdownFile(InputStream source, File target, MarkdownOutputStrategy strategy) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        writeToFile(csv2Markdown(source, strategy), target);
    }

    /* ── 输出层：写出到文件 ─────────────────────────────────────────── */

    private static void writeToFile(String content, File target) throws Excel2MarkdownException {
        /* 目标目录不存在时自动创建 */
        File parentDir = target.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new Excel2MarkdownException("写出文件失败: " + target.getAbsolutePath(), e);
        }
    }
}
