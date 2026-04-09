package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class XlsParser {

    private final SheetRowsNormalizer sheetRowsNormalizer = new SheetRowsNormalizer();

    /**
     * 解析 xls 输入流，对每个选中的 sheet 回调 (sheetName, rows)。
     */
    void parse(InputStream inputStream, ConvertConfig config,
               BiConsumer<String, List<List<String>>> sheetConsumer) throws Excel2MarkdownException {
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        try (HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {
            DataFormatter dataFormatter = new DataFormatter();
            int sheetCount = workbook.getNumberOfSheets();

            for (int i = 0; i < sheetCount; i++) {
                String sheetName = workbook.getSheetName(i);
                if (!isSheetSelected(sheetName, i, config)) {
                    continue;
                }

                Sheet sheet = workbook.getSheetAt(i);
                List<List<String>> rows = new ArrayList<>();
                int previousRowNum = -1;

                for (Row row : sheet) {
                    if (row == null) {
                        continue;
                    }
                    for (int missingRowNum = previousRowNum + 1; missingRowNum < row.getRowNum(); missingRowNum++) {
                        rows.add(new ArrayList<>());
                    }
                    List<String> cells = new ArrayList<>();
                    int lastCellNum = row.getLastCellNum();
                    for (int c = 0; c < lastCellNum; c++) {
                        Cell cell = row.getCell(c);
                        cells.add(cell == null ? "" : dataFormatter.formatCellValue(cell));
                    }
                    rows.add(cells);
                    previousRowNum = row.getRowNum();
                }

                sheetConsumer.accept(sheetName, sheetRowsNormalizer.normalize(rows));
            }
        } catch (Exception e) {
            throw new Excel2MarkdownException("解析 xls 失败: " + e.getMessage(), e);
        }
    }

    private boolean isSheetSelected(String sheetName, int sheetIndex, ConvertConfig config) {
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
}
