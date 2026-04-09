package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class CsvLineParser {

    void parse(InputStream inputStream, String encoding, Consumer<List<String>> rowConsumer) throws Excel2MarkdownException {
        /* fast-fail */
        if (inputStream == null) {
            throw new Excel2MarkdownException("inputStream 不能为 null");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(encoding)))) {
            List<String> currentRow = new ArrayList<>();
            StringBuilder currentField = new StringBuilder();
            boolean inQuotes = false;
            int ch;

            /* RFC 4180 状态机：逐字符读取，处理引号包裹与字段内换行 */
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;

                if (inQuotes) {
                    if (c == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            currentField.append('"');
                        } else {
                            inQuotes = false;
                            if (next == ',') {
                                currentRow.add(currentField.toString());
                                currentField.setLength(0);
                            } else if (next == '\r') {
                                int afterCr = reader.read();
                                if (afterCr != '\n' && afterCr != -1) {
                                    currentField.append((char) afterCr);
                                }
                                currentRow.add(currentField.toString());
                                currentField.setLength(0);
                                rowConsumer.accept(new ArrayList<>(currentRow));
                                currentRow.clear();
                            } else if (next == '\n') {
                                currentRow.add(currentField.toString());
                                currentField.setLength(0);
                                rowConsumer.accept(new ArrayList<>(currentRow));
                                currentRow.clear();
                            } else if (next != -1) {
                                currentField.append((char) next);
                            } else {
                                currentRow.add(currentField.toString());
                                currentField.setLength(0);
                            }
                        }
                    } else {
                        currentField.append(c);
                    }
                } else {
                    if (c == '"') {
                        inQuotes = true;
                    } else if (c == ',') {
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                    } else if (c == '\r') {
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            currentField.append((char) next);
                        }
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                        rowConsumer.accept(new ArrayList<>(currentRow));
                        currentRow.clear();
                    } else if (c == '\n') {
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                        rowConsumer.accept(new ArrayList<>(currentRow));
                        currentRow.clear();
                    } else {
                        currentField.append(c);
                    }
                }
            }

            /* 文件末尾无换行时刷出最后一行 */
            if (currentField.length() > 0 || !currentRow.isEmpty()) {
                currentRow.add(currentField.toString());
                rowConsumer.accept(new ArrayList<>(currentRow));
            }
        } catch (IOException e) {
            throw new Excel2MarkdownException("读取 CSV 流失败: " + e.getMessage(), e);
        }
    }
}
