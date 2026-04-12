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
    <version>1.2.0</version>
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

All methods have overloaded variants accepting `MarkdownOutputStrategy`. When omitted, `WYSIWYG` is used.

### String output (in-memory)

| Method | Input | Output |
|--------|-------|--------|
| `Excel2MarkdownUtils.xlsx2Markdown(File file)` | `.xlsx` file | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.xlsx2Markdown(File file, MarkdownOutputStrategy strategy)` | `.xlsx` file | `String` (Markdown, chosen strategy) |
| `Excel2MarkdownUtils.xlsx2Markdown(InputStream inputStream)` | `.xlsx` stream | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.xlsx2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy)` | `.xlsx` stream | `String` (Markdown, chosen strategy) |
| `Excel2MarkdownUtils.xls2Markdown(File file)` | `.xls` file | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.xls2Markdown(File file, MarkdownOutputStrategy strategy)` | `.xls` file | `String` (Markdown, chosen strategy) |
| `Excel2MarkdownUtils.xls2Markdown(InputStream inputStream)` | `.xls` stream | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.xls2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy)` | `.xls` stream | `String` (Markdown, chosen strategy) |
| `Excel2MarkdownUtils.csv2Markdown(File file)` | `.csv` file | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.csv2Markdown(File file, MarkdownOutputStrategy strategy)` | `.csv` file | `String` (Markdown, chosen strategy) |
| `Excel2MarkdownUtils.csv2Markdown(InputStream inputStream)` | `.csv` stream | `String` (Markdown, WYSIWYG) |
| `Excel2MarkdownUtils.csv2Markdown(InputStream inputStream, MarkdownOutputStrategy strategy)` | `.csv` stream | `String` (Markdown, chosen strategy) |

### File output (writes UTF-8 `.md` file)

| Method | Behavior |
|--------|----------|
| `xlsx2MarkdownFile(File source, File target)` | Converts `source` and writes to `target` (WYSIWYG) |
| `xlsx2MarkdownFile(File source, File target, MarkdownOutputStrategy strategy)` | Converts `source` and writes to `target` (chosen strategy) |
| `xlsx2MarkdownFile(InputStream source, File target)` | Same for stream input (WYSIWYG) |
| `xlsx2MarkdownFile(InputStream source, File target, MarkdownOutputStrategy strategy)` | Same for stream input (chosen strategy) |
| `xls2MarkdownFile(...)` | Same variants for `.xls` |
| `csv2MarkdownFile(...)` | Same variants for `.csv` |

All file-output methods auto-create parent directories if missing.

## 5. Common Usage Patterns

### Basic one-liner (WYSIWYG, default)

```java
String md = Excel2MarkdownUtils.xlsx2Markdown(new File("report.xlsx"));
```

### With native coordinates

```java
String md = Excel2MarkdownUtils.xlsx2Markdown(
    new File("report.xlsx"),
    MarkdownOutputStrategy.NATIVE_COORDINATES
);
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

Two strategies control how the Markdown table is rendered. Choose based on your use case.

### Strategy: WYSIWYG (default)

The first row of data becomes the Markdown table header. No column letters or row numbers are added. Use this when you want the output to look like the original spreadsheet.

Knowing these rules lets you predict the Markdown shape exactly:

1. **Whole-sheet single table** — Every sheet produces exactly one Markdown table.
2. **First data row is the header** — The first row of data becomes the Markdown table header row. No synthetic column-letter header is added.
3. **Original column positions preserved** — Leading blank columns are not stripped.
4. **Empty rows preserved** — Blank rows appear as empty table rows.
5. **Newlines in cells** — Converted to `<br>` so Markdown table syntax stays valid.
6. **Pipe characters (`|`) in cells** — Escaped automatically so they do not break table structure.
7. **Encoding** — Output is always UTF-8.

### Strategy: NATIVE_COORDINATES

Each table is prefixed with a one-line English note. The header row uses Excel column letters (`A`, `B`, `C`, …). Every data row is prefixed with a 1-based row number. The top-left cell is `Rows`.

Example output shape:

```md
> Note: Column headers use Excel column letters (A, B, C ...). Row numbers reflect the native row index starting from 1.

| Rows | A | B | C |
| --- | --- | --- | --- |
| 1 | header1 | header2 | header3 |
| 2 | value1  | value2  | value3  |
```

Use this when callers need to reference cells by their original spreadsheet coordinates, or when feeding the table to an LLM that should be able to locate specific cells.

## 7. Exception Model

All public methods on `Excel2MarkdownUtils` throw a single checked exception:

```java
cn.creekmoon.excel2markdown.exception.Excel2MarkdownException
```

Typical causes:
- `null` arguments
- File does not exist
- IO failure while reading or writing

## 8. Public Configuration

`ConvertConfig` exists internally but is **not exposed** through `Excel2MarkdownUtils`. Sheet filters, encoding, and formula evaluation are not available through the public utility surface.

`MarkdownOutputStrategy` is a **public enum** that controls table rendering. Pass it as the last argument to any conversion method to switch between `WYSIWYG` (default) and `NATIVE_COORDINATES`.

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
