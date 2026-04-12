package cn.creekmoon.excel2markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SheetRowsNormalizer {

    /**
     * Trims only trailing empty cells from each row.
     * Leading empty columns are preserved so that column indices stay aligned
     * with the original Excel column letters (A, B, C, ...).
     */
    List<List<String>> normalize(List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows;
        }

        List<List<String>> normalizedRows = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            if (row == null || row.isEmpty()) {
                normalizedRows.add(Collections.emptyList());
                continue;
            }
            int toIndex = row.size();
            while (toIndex > 0 && isBlank(row.get(toIndex - 1))) {
                toIndex--;
            }
            if (toIndex == 0) {
                normalizedRows.add(Collections.emptyList());
            } else {
                normalizedRows.add(new ArrayList<>(row.subList(0, toIndex)));
            }
        }
        return normalizedRows;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
