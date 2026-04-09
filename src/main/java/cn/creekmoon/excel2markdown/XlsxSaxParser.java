package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class XlsxSaxParser {

    private final SheetRowsNormalizer sheetRowsNormalizer = new SheetRowsNormalizer();

    /**
     * 解析 xlsx 输入流，对每个选中的 sheet 回调 (sheetName, rows)。
     * rows 为 List<List<String>>，每个内部 List 是一行的格式化显示值。
     */
    void parse(InputStream inputStream, ConvertConfig config,
               BiConsumer<String, List<List<String>>> sheetConsumer) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        try (OPCPackage pkg = OPCPackage.open(inputStream)) {
            /* 构建 XSSFReader 和共享资源 */
            XSSFReader xssfReader = new XSSFReader(pkg);
            SharedStrings sharedStrings = xssfReader.getSharedStringsTable();
            StylesTable stylesTable = xssfReader.getStylesTable();
            DataFormatter dataFormatter = new DataFormatter();
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            int sheetIndex = 0;
            while (sheetIterator.hasNext()) {
                try (InputStream sheetStream = sheetIterator.next()) {
                    String sheetName = sheetIterator.getSheetName();

                    /* 按 config 过滤 sheet */
                    if (!isSheetSelected(sheetName, sheetIndex, config)) {
                        sheetIndex++;
                        continue;
                    }

                    /* SAX 解析当前 sheet */
                    List<List<String>> rows = parseSheet(sheetStream, sharedStrings, stylesTable, dataFormatter, config);
                    sheetConsumer.accept(sheetName, rows);
                    sheetIndex++;
                }
            }
        } catch (Excel2MarkdownException e) {
            throw e;
        } catch (Exception e) {
            throw new Excel2MarkdownException("解析 xlsx 失败: " + e.getMessage(), e);
        }
    }

    private List<List<String>> parseSheet(InputStream sheetStream, SharedStrings sharedStrings,
                                           StylesTable stylesTable, DataFormatter dataFormatter,
                                           ConvertConfig config) throws Excel2MarkdownException {
        List<List<String>> rows = new ArrayList<>();
        RowCollector rowCollector = new RowCollector(rows);

        try {
            XMLReader sheetParser = XMLHelper.newXMLReader();
            XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(
                    stylesTable, sharedStrings, rowCollector, dataFormatter,
                    config.forceEvaluateFormulas
            );
            sheetParser.setContentHandler(handler);
            sheetParser.parse(new InputSource(sheetStream));
        } catch (Exception e) {
            throw new Excel2MarkdownException("解析 sheet 内容失败: " + e.getMessage(), e);
        }
        return sheetRowsNormalizer.normalize(rows);
    }

    private boolean isSheetSelected(String sheetName, int sheetIndex, ConvertConfig config) {
        /* 未配置任何过滤条件时，选中全部 sheet */
        if ((config.sheetNames == null || config.sheetNames.isEmpty())
                && (config.sheetIndexes == null || config.sheetIndexes.isEmpty())) {
            return true;
        }
        if (config.sheetNames != null && config.sheetNames.contains(sheetName)) {
            return true;
        }
        if (config.sheetIndexes != null && config.sheetIndexes.contains(sheetIndex)) {
            return true;
        }
        return false;
    }

    /**
     * SAX 事件回调，将每行单元格收集为 List<String>。
     * 负责根据列引用（如 A、B、D）填充跳过的空列。
     */
    private static class RowCollector implements SheetContentsHandler {

        private final List<List<String>> rows;
        private List<String> currentRow;
        private int currentColIndex;
        private int previousRowNum = -1;

        RowCollector(List<List<String>> rows) {
            this.rows = rows;
        }

        @Override
        public void startRow(int rowNum) {
            /* 补齐缺失的空行，供渲染层切分文本块/表格块 */
            for (int i = previousRowNum + 1; i < rowNum; i++) {
                rows.add(new ArrayList<>());
            }
            currentRow = new ArrayList<>();
            currentColIndex = 0;
        }

        @Override
        public void endRow(int rowNum) {
            rows.add(new ArrayList<>(currentRow));
            currentRow = null;
            previousRowNum = rowNum;
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            /* cellReference 为 null 时跳过 */
            if (cellReference == null) {
                return;
            }
            int targetColIndex = parseColumnIndex(cellReference);

            /* 填充跳过的空单元格，保持列对齐 */
            while (currentColIndex < targetColIndex) {
                currentRow.add("");
                currentColIndex++;
            }
            currentRow.add(formattedValue == null ? "" : formattedValue);
            currentColIndex++;
        }

        private int parseColumnIndex(String cellReference) {
            /* 提取列字母部分（如 "AB12" → "AB"），转换为 0-based 列下标 */
            int col = 0;
            for (int i = 0; i < cellReference.length(); i++) {
                char c = cellReference.charAt(i);
                if (!Character.isLetter(c)) {
                    break;
                }
                col = col * 26 + (Character.toUpperCase(c) - 'A' + 1);
            }
            return col - 1;
        }
    }
}
