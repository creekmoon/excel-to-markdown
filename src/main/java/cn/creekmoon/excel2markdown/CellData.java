package cn.creekmoon.excel2markdown;

class CellData {

    final String rawValue;
    final String formatString;
    final boolean isNumeric;

    CellData(String rawValue, String formatString, boolean isNumeric) {
        this.rawValue = rawValue;
        this.formatString = formatString;
        this.isNumeric = isNumeric;
    }

    static CellData text(String value) {
        return new CellData(value, null, false);
    }

    static CellData numeric(String rawValue, String formatString) {
        return new CellData(rawValue, formatString, true);
    }
}
