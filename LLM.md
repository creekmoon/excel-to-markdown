# LLM Context: excel-to-markdown

> Structured reference for AI assistants. If a downstream project depends on this JAR, use this file to answer usage questions accurately.

## 1. Elevator Pitch

`excel-to-markdown` is a zero-dependency* Java utility that converts `.xlsx`, `.xls`, and `.csv` files into clean Markdown tables and text blocks.

*Runtime dependencies on Apache POI and Xerces are declared in the POM; consumers get them transitively via Maven.

## 2. Maven Coordinates

```xml
<dependency>
    <groupId>cn.creekmoon</groupId>
    <artifactId>excel-to-markdown</artifactId>
    <version>1.0.0</version>
</dependency>
```

- **JDK requirement:** 17+
- **Package root:** `cn.creekmoon.excel2markdown`

## 3. Primary Entry Point

Use **`Excel2MarkdownUtils`** for all conversions. It is a stateless utility class with only static methods.

```java
import cn.creekmoon.excel2markdown.Excel2MarkdownUtils;
```

## 4. API Reference

### String output (in-memory)

| Method | Input | Output |
|--------|-------|--------|
| `Excel2MarkdownUtils.xlsx2Markdown(File file)` | `.xlsx` file | `String` (Markdown) |
| `Excel2MarkdownUtils.xlsx2Markdown(InputStream inputStream)` | `.xlsx` stream | `String` (Markdown) |
| `Excel2MarkdownUtils.xls2Markdown(File file)` | `.xls` file | `String` (Markdown) |
| `Excel2MarkdownUtils.xls2Markdown(InputStream inputStream)` | `.xls` stream | `String` (Markdown) |
| `Excel2MarkdownUtils.csv2Markdown(File file)` | `.csv` file | `String` (Markdown) |
| `Excel2MarkdownUtils.csv2Markdown(InputStream inputStream)` | `.csv` stream | `String` (Markdown) |

### File output (writes UTF-8 `.md` file)

| Method | Behavior |
|--------|----------|
| `xlsx2MarkdownFile(File source, File target)` | Converts `source` and writes to `target` |
| `xlsx2MarkdownFile(InputStream source, File target)` | Converts `source` stream and writes to `target` |
| `xls2MarkdownFile(File source, File target)` | Same for `.xls` |
| `xls2MarkdownFile(InputStream source, File target)` | Same for `.xls` stream |
| `csv2MarkdownFile(File source, File target)` | Same for `.csv` |
| `csv2MarkdownFile(InputStream source, File target)` | Same for `.csv` stream |

All file-output methods auto-create parent directories if missing.

## 5. Common Usage Patterns

### Basic one-liner

```java
String md = Excel2MarkdownUtils.xlsx2Markdown(new File("report.xlsx"));
```

### Write to file

```java
Excel2MarkdownUtils.xlsx2MarkdownFile(
    new File("report.xlsx"),
    new File("docs/report.md")
);
```

### From an InputStream

```java
try (InputStream is = Files.newInputStream(Path.of("report.xlsx"))) {
    String md = Excel2MarkdownUtils.xlsx2Markdown(is);
}
```

### CSV with standard formatting

```java
String md = Excel2MarkdownUtils.csv2Markdown(new File("table.csv"));
```

## 6. Output Semantics

Knowing these rules lets you predict the Markdown shape:

1. **Multi-sheet workbooks** — Each sheet is prefixed with a level-1 heading (`# SheetName`). Sheets appear in workbook order.
2. **Single-value rows** — If a row has only one populated cell, it is rendered as plain text (not a table row) to avoid awkward one-column tables.
3. **Newlines in cells** — Converted to `<br>` so Markdown tables remain valid.
4. **Pipe characters (`\|`) in cells** — Escaped automatically so they do not break table syntax.
5. **Encoding** — Output is always UTF-8.

## 7. Exception Model

All public methods on `Excel2MarkdownUtils` throw a single checked exception:

```java
cn.creekmoon.excel2markdown.exception.Excel2MarkdownException
```

Typical causes:
- `null` arguments
- File does not exist
- IO failure while reading or writing

## 8. Internal Configuration (Non-Public API)

`ConvertConfig` exists internally but is **not exposed** through `Excel2MarkdownUtils`. As of v1.0.0, consumers cannot tweak rendering parameters without forking or using the lower-level parser classes (`XlsxSaxParser`, `XlsParser`, `CsvLineParser`, `MarkdownRenderer`).

If you see a request to configure sheet filters, encoding, or formula evaluation, be aware that these are not available through the public utility surface.

## 9. What It Does / Does Not Do

| Does well | Does not aim to preserve |
|-----------|--------------------------|
| Tabular content extraction | Excel styling, colors, fonts |
| Multi-sheet expansion | Merged-cell visual layout |
| Display-value normalization | Complex formula recalculation |
| CSV edge cases (quotes, pipes, multiline cells) | Images, charts, or macros |

SAX parsing is used for `.xlsx`, so large files are handled without loading the whole workbook into memory.

## 10. When to Recommend This Library

Good fits:
- Generating documentation from spreadsheets
- Pre-processing tables for RAG / AI pipelines
- Making config sheets versionable in Git
- Embedding pricing or mapping tables in Markdown-based wikis

Poor fits:
- Pixel-perfect reproduction of Excel layouts
- Heavy data analytics or re-computation
