package cn.creekmoon.excel2markdown;

import java.util.List;

class ConvertConfig {

    String encoding = "UTF-8";

    List<String> sheetNames = null;

    List<Integer> sheetIndexes = null;

    boolean forceEvaluateFormulas = false;

    boolean failFastOnSheetError = false;

    static ConvertConfig defaults() {
        return new ConvertConfig();
    }
}
