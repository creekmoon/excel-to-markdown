package cn.creekmoon.excel2markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SheetRowsNormalizer {

    List<List<String>> normalize(List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows;
        }

        /* 去掉整张 sheet 共同的左侧留白列，再裁掉每行尾部空列 */
        int leadingEmptyColumnCount = resolveLeadingEmptyColumnCount(rows);
        List<List<String>> normalizedRows = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            if (row == null || row.isEmpty()) {
                normalizedRows.add(Collections.emptyList());
                continue;
            }
            int fromIndex = Math.min(leadingEmptyColumnCount, row.size());
            int toIndex = row.size();
            while (toIndex > fromIndex && isBlank(row.get(toIndex - 1))) {
                toIndex--;
            }
            if (fromIndex >= toIndex) {
                normalizedRows.add(Collections.emptyList());
                continue;
            }
            normalizedRows.add(new ArrayList<>(row.subList(fromIndex, toIndex)));
        }
        return normalizedRows;
    }

    private int resolveLeadingEmptyColumnCount(List<List<String>> rows) {
        int minFirstNonBlankIndex = Integer.MAX_VALUE;
        for (List<String> row : rows) {
            int firstNonBlankIndex = resolveFirstNonBlankIndex(row);
            if (firstNonBlankIndex >= 0) {
                minFirstNonBlankIndex = Math.min(minFirstNonBlankIndex, firstNonBlankIndex);
            }
        }
        return minFirstNonBlankIndex == Integer.MAX_VALUE ? 0 : minFirstNonBlankIndex;
    }

    private int resolveFirstNonBlankIndex(List<String> row) {
        if (row == null || row.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < row.size(); i++) {
            if (!isBlank(row.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
