package cn.creekmoon.excel2markdown;

import org.apache.poi.ss.usermodel.DataFormatter;

class DisplayFormatter {

    private final DataFormatter dataFormatter = new DataFormatter();

    String format(CellData cell) {
        if (!cell.isNumeric || cell.formatString == null || cell.formatString.isEmpty()) {
            return cell.rawValue == null ? "" : cell.rawValue;
        }
        try {
            double numericValue = Double.parseDouble(cell.rawValue);
            return dataFormatter.formatRawCellContents(numericValue, 0, cell.formatString);
        } catch (NumberFormatException e) {
            /* 无法解析为数字时直传原始值 */
            return cell.rawValue == null ? "" : cell.rawValue;
        }
    }
}
