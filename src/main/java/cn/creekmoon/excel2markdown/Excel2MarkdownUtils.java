package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Excel2MarkdownUtils {

    private Excel2MarkdownUtils() {
    }

    /* ── xlsx → String ─────────────────────────────────────────────── */

    public static String xlsx2Markdown(File file) throws Excel2MarkdownException {
        /* fast-fail */
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return xlsx2Markdown(is);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    public static String xlsx2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        /* 解析并渲染 */
        MarkdownRenderer renderer = new MarkdownRenderer();
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

    public static String xls2Markdown(File file) throws Excel2MarkdownException {
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return xls2Markdown(is);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    public static String xls2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        MarkdownRenderer renderer = new MarkdownRenderer();
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

    public static void xls2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = xls2Markdown(source);
        writeToFile(markdown, target);
    }

    public static void xls2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = xls2Markdown(source);
        writeToFile(markdown, target);
    }

    /* ── csv → String ──────────────────────────────────────────────── */

    public static String csv2Markdown(File file) throws Excel2MarkdownException {
        /* fast-fail */
        if (file == null) {
            throw new Excel2MarkdownException("file 不能为 null");
        }
        if (!file.exists()) {
            throw new Excel2MarkdownException("文件不存在: " + file.getAbsolutePath());
        }

        try (InputStream is = new FileInputStream(file)) {
            return csv2Markdown(is);
        } catch (IOException e) {
            throw new Excel2MarkdownException("打开文件失败: " + file.getAbsolutePath(), e);
        }
    }

    public static String csv2Markdown(InputStream inputStream) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        /* 解析并渲染 */
        MarkdownRenderer renderer = new MarkdownRenderer();
        ConvertConfig config = ConvertConfig.defaults();
        CsvLineParser parser = new CsvLineParser();
        parser.parse(inputStream, config.encoding, renderer::addRow);
        return renderer.render();
    }

    /* ── xlsx → File ───────────────────────────────────────────────── */

    public static void xlsx2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = xlsx2Markdown(source);
        writeToFile(markdown, target);
    }

    public static void xlsx2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = xlsx2Markdown(source);
        writeToFile(markdown, target);
    }

    /* ── csv → File ────────────────────────────────────────────────── */

    public static void csv2MarkdownFile(File source, File target) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = csv2Markdown(source);
        writeToFile(markdown, target);
    }

    public static void csv2MarkdownFile(InputStream source, File target) throws Excel2MarkdownException {
        /* fast-fail */
        if (source == null) {
            throw new Excel2MarkdownException("source 不能为 null");
        }
        if (target == null) {
            throw new Excel2MarkdownException("target 不能为 null");
        }

        String markdown = csv2Markdown(source);
        writeToFile(markdown, target);
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
