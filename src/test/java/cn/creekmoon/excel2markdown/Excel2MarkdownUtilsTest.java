package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class Excel2MarkdownUtilsTest {

    // ─────────────────────────────────────────────────────────────────────────
    // 辅助方法
    // ─────────────────────────────────────────────────────────────────────────

    /** 从 classpath 加载测试资源文件（正确处理路径中的中文字符） */
    private File classpathFile(String name) {
        try {
            return new File(getClass().getClassLoader().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从 classpath 打开测试资源输入流 */
    private InputStream classpathStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * 将 markdown 内容保存到 target/test-output/<filename>，
     * 并在控制台打印绝对路径，方便人工核对。
     */
    private void saveOutput(String content, String filename) throws IOException {
        Path outDir = Paths.get("target", "test-output");
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve(filename);
        Files.writeString(outFile, content, StandardCharsets.UTF_8);
        System.out.println("[test-output] " + outFile.toAbsolutePath());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xlsx2Markdown(File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xlsx2Markdown_file_null_throwsException() {
        Excel2MarkdownException ex = assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2Markdown((File) null)
        );
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void xlsx2Markdown_file_notExist_throwsException() {
        File missing = new File("not_exist_file.xlsx");
        Excel2MarkdownException ex = assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2Markdown(missing)
        );
        assertTrue(ex.getMessage().contains("不存在") || ex.getMessage().contains("not_exist_file"));
    }

    @Test
    void xlsx2Markdown_file_validXlsx_returnMarkdown() throws Exception {
        File xlsx = classpathFile("sample2.xlsx");
        String result = Excel2MarkdownUtils.xlsx2Markdown(xlsx);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("|"));
        assertTrue(result.contains("---"));
        saveOutput(result, "sample2.xlsx.md");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xlsx2Markdown(InputStream)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xlsx2Markdown_stream_null_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2Markdown((InputStream) null)
        );
    }

    @Test
    void xlsx2Markdown_stream_validXlsx_returnMarkdown() throws Exception {
        try (InputStream is = classpathStream("sample2.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            assertNotNull(result);
            assertFalse(result.isBlank());
            assertTrue(result.contains("|"));
            assertTrue(result.contains("---"));
            saveOutput(result, "sample2-stream.xlsx.md");
        }
    }

    @Test
    void xlsx2Markdown_stream_corruptedData_throwsException() {
        // 伪造非法 xlsx 字节流，应抛出异常而不是静默失败
        byte[] garbage = "THIS IS NOT A VALID XLSX FILE".getBytes(StandardCharsets.UTF_8);
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2Markdown(new ByteArrayInputStream(garbage))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // csv2Markdown(File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void csv2Markdown_file_null_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2Markdown((File) null)
        );
    }

    @Test
    void csv2Markdown_file_notExist_throwsException() {
        File missing = new File("not_exist_file.csv");
        Excel2MarkdownException ex = assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2Markdown(missing)
        );
        assertTrue(ex.getMessage().contains("不存在") || ex.getMessage().contains("not_exist_file"));
    }

    @Test
    void csv2Markdown_file_validCsv_returnMarkdown() throws Exception {
        File csv = classpathFile("sample.csv");
        String result = Excel2MarkdownUtils.csv2Markdown(csv);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("|"));
        assertTrue(result.contains("---"));
        assertTrue(result.contains("姓名"));
        assertTrue(result.contains("张三"));
        saveOutput(result, "sample.csv.md");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // csv2Markdown(InputStream)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void csv2Markdown_stream_null_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2Markdown((InputStream) null)
        );
    }

    @Test
    void csv2Markdown_stream_validCsv_returnMarkdown() throws Exception {
        try (InputStream is = classpathStream("sample.csv")) {
            String result = Excel2MarkdownUtils.csv2Markdown(is);

            assertNotNull(result);
            assertTrue(result.contains("|"));
            assertTrue(result.contains("---"));
            assertTrue(result.contains("姓名"));
            saveOutput(result, "sample-stream.csv.md");
        }
    }

    @Test
    void csv2Markdown_stream_pipeCharInCell_escapedInOutput() throws Exception {
        try (InputStream is = classpathStream("sample_pipe.csv")) {
            String result = Excel2MarkdownUtils.csv2Markdown(is);

            assertNotNull(result);
            assertTrue(result.contains("\\|"), "管道符应被转义为 \\|");
            saveOutput(result, "sample_pipe.csv.md");
        }
    }

    @Test
    void csv2Markdown_stream_multilineCell_replacedWithBr() throws Exception {
        // 单元格内含换行，输出中应替换为 <br>
        try (InputStream is = classpathStream("sample_pipe.csv")) {
            String result = Excel2MarkdownUtils.csv2Markdown(is);

            assertNotNull(result);
            assertTrue(result.contains("<br>"), "单元格内换行应替换为 <br>");
        }
    }

    @Test
    void csv2Markdown_stream_singleRow_onlyHeaderAndSeparator() throws Exception {
        // 新语义：列字母表头行 + 分隔行 + 1 条数据行 = 3 行
        String csvContent = "A,B,C\n";
        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        String result = Excel2MarkdownUtils.csv2Markdown(is);

        long rowCount = result.lines().filter(l -> l.startsWith("|")).count();
        // 期望：列字母表头行 + 分隔行 + 数据行 = 3 行
        assertEquals(3, rowCount);
    }

    @Test
    void csv2Markdown_stream_emptyStream_returnsEmpty() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        String result = Excel2MarkdownUtils.csv2Markdown(is);

        // 空输入应返回空字符串，而不是抛异常
        assertNotNull(result);
        assertTrue(result.isBlank());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xlsx2MarkdownFile(File, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xlsx2MarkdownFile_sourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2MarkdownFile((File) null, target)
        );
    }

    @Test
    void xlsx2MarkdownFile_targetNull_throwsException() {
        File source = classpathFile("sample2.xlsx");
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2MarkdownFile(source, null)
        );
    }

    @Test
    void xlsx2MarkdownFile_validInput_writesMarkdownFile(@TempDir Path tmpDir) throws Exception {
        File source = classpathFile("sample2.xlsx");
        File target = tmpDir.resolve("output.md").toFile();

        Excel2MarkdownUtils.xlsx2MarkdownFile(source, target);

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertFalse(content.isBlank());
        assertTrue(content.contains("|"));
        assertTrue(content.contains("---"));
    }

    @Test
    void xlsx2MarkdownFile_targetDirNotExist_autoCreatesDir(@TempDir Path tmpDir) throws Exception {
        File source = classpathFile("sample2.xlsx");
        // 目标目录为不存在的子目录
        File target = tmpDir.resolve("subdir/nested/output.md").toFile();

        Excel2MarkdownUtils.xlsx2MarkdownFile(source, target);

        assertTrue(target.exists(), "父目录应被自动创建");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xlsx2MarkdownFile(InputStream, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xlsx2MarkdownFile_streamSourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2MarkdownFile((InputStream) null, target)
        );
    }

    @Test
    void xlsx2MarkdownFile_streamTargetNull_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xlsx2MarkdownFile(classpathStream("sample2.xlsx"), null)
        );
    }

    @Test
    void xlsx2MarkdownFile_streamValidInput_writesFile(@TempDir Path tmpDir) throws Exception {
        File target = tmpDir.resolve("output.md").toFile();
        try (InputStream is = classpathStream("sample2.xlsx")) {
            Excel2MarkdownUtils.xlsx2MarkdownFile(is, target);
        }

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertFalse(content.isBlank());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // csv2MarkdownFile(File, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void csv2MarkdownFile_sourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2MarkdownFile((File) null, target)
        );
    }

    @Test
    void csv2MarkdownFile_targetNull_throwsException() {
        File source = classpathFile("sample.csv");
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2MarkdownFile(source, null)
        );
    }

    @Test
    void csv2MarkdownFile_validInput_writesMarkdownFile(@TempDir Path tmpDir) throws Exception {
        File source = classpathFile("sample.csv");
        File target = tmpDir.resolve("output.md").toFile();

        Excel2MarkdownUtils.csv2MarkdownFile(source, target);

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertFalse(content.isBlank());
        assertTrue(content.contains("姓名"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // csv2MarkdownFile(InputStream, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void csv2MarkdownFile_streamSourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2MarkdownFile((InputStream) null, target)
        );
    }

    @Test
    void csv2MarkdownFile_streamTargetNull_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.csv2MarkdownFile(classpathStream("sample.csv"), null)
        );
    }

    @Test
    void csv2MarkdownFile_streamValidInput_writesFile(@TempDir Path tmpDir) throws Exception {
        File target = tmpDir.resolve("output.md").toFile();
        try (InputStream is = classpathStream("sample.csv")) {
            Excel2MarkdownUtils.csv2MarkdownFile(is, target);
        }

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("|"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 输出格式正确性
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void csv2Markdown_outputFormat_headerSeparatorDataRowOrder() throws Exception {
        // 新语义：第 1 行是列字母表头（A, B），第 2 行是分隔行，第 3 行起是原始数据
        String csvContent = "Name,Score\nAlice,90\nBob,85\n";
        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        String result = Excel2MarkdownUtils.csv2Markdown(is);

        String[] lines = result.lines().filter(l -> !l.isBlank()).toArray(String[]::new);
        // 第 1 行：列字母表头（A、B）
        assertTrue(lines[0].contains("| A |") || lines[0].startsWith("| A "), "first row must be column letter header");
        // 第 2 行：分隔行（全是 ---）
        assertTrue(lines[1].matches("\\|( --- \\|)+"));
        // 第 3 行：原 CSV 第一行数据（Name, Score）
        assertTrue(lines[2].contains("Name") && lines[2].contains("Score"));
        // 第 4、5 行：后续数据
        assertTrue(lines[3].contains("Alice"));
        assertTrue(lines[4].contains("Bob"));
    }

    @Test
    void csv2Markdown_outputFormat_columnCountMatchesHeader() throws Exception {
        String csvContent = "A,B,C\n1,2,3\n";
        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        String result = Excel2MarkdownUtils.csv2Markdown(is);

        // 每一行的列数应一致（通过 | 的数量判断）
        result.lines().filter(l -> l.startsWith("|")).forEach(line -> {
            long pipeCount = line.chars().filter(c -> c == '|').count();
            // 3 列 → 4 个 |
            assertEquals(4, pipeCount, "列数不一致: " + line);
        });
    }

    @Test
    void xlsx2Markdown_multiSheet_eachSheetHasHeader() throws Exception {
        // sample2.xlsx 应至少有 1 个 sheet，输出中包含 # 标题行
        File xlsx = classpathFile("sample2.xlsx");
        String result = Excel2MarkdownUtils.xlsx2Markdown(xlsx);

        // 多 sheet 时，每个 sheet 都以 # 开头
        assertTrue(result.contains("# "), "每个 sheet 应有 # 标题");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xls2Markdown(File) — sample1.xls
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xls2Markdown_file_null_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2Markdown((File) null)
        );
    }

    @Test
    void xls2Markdown_file_notExist_throwsException() {
        File missing = new File("not_exist_file.xls");
        Excel2MarkdownException ex = assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2Markdown(missing)
        );
        assertTrue(ex.getMessage().contains("不存在") || ex.getMessage().contains("not_exist_file"));
    }

    @Test
    void xls2Markdown_file_validXls_returnMarkdown() throws Exception {
        File xls = classpathFile("sample1.xls");
        String result = Excel2MarkdownUtils.xls2Markdown(xls);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("|"));
        assertTrue(result.contains("---"));
        saveOutput(result, "sample1.xls.md");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xls2Markdown(InputStream)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xls2Markdown_stream_null_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2Markdown((InputStream) null)
        );
    }

    @Test
    void xls2Markdown_stream_validXls_returnMarkdown() throws Exception {
        try (InputStream is = classpathStream("sample1.xls")) {
            String result = Excel2MarkdownUtils.xls2Markdown(is);

            assertNotNull(result);
            assertFalse(result.isBlank());
            assertTrue(result.contains("|"));
            assertTrue(result.contains("---"));
            saveOutput(result, "sample1-stream.xls.md");
        }
    }

    @Test
    void xls2Markdown_stream_corruptedData_throwsException() {
        byte[] garbage = "NOT A VALID XLS FILE".getBytes(StandardCharsets.UTF_8);
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2Markdown(new ByteArrayInputStream(garbage))
        );
    }

    @Test
    void xls2Markdown_multiSheet_eachSheetHasHeader() throws Exception {
        File xls = classpathFile("sample1.xls");
        String result = Excel2MarkdownUtils.xls2Markdown(xls);

        assertTrue(result.contains("# "), "每个 sheet 应有 # 标题");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xls2MarkdownFile(File, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xls2MarkdownFile_sourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2MarkdownFile((File) null, target)
        );
    }

    @Test
    void xls2MarkdownFile_targetNull_throwsException() {
        File source = classpathFile("sample1.xls");
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2MarkdownFile(source, null)
        );
    }

    @Test
    void xls2MarkdownFile_validInput_writesMarkdownFile(@TempDir Path tmpDir) throws Exception {
        File source = classpathFile("sample1.xls");
        File target = tmpDir.resolve("output.md").toFile();

        Excel2MarkdownUtils.xls2MarkdownFile(source, target);

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertFalse(content.isBlank());
        assertTrue(content.contains("|"));
        assertTrue(content.contains("---"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xls2MarkdownFile(InputStream, File)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xls2MarkdownFile_streamSourceNull_throwsException(@TempDir Path tmpDir) {
        File target = tmpDir.resolve("out.md").toFile();
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2MarkdownFile((InputStream) null, target)
        );
    }

    @Test
    void xls2MarkdownFile_streamTargetNull_throwsException() {
        assertThrows(
                Excel2MarkdownException.class,
                () -> Excel2MarkdownUtils.xls2MarkdownFile(classpathStream("sample1.xls"), null)
        );
    }

    @Test
    void xls2MarkdownFile_streamValidInput_writesFile(@TempDir Path tmpDir) throws Exception {
        File target = tmpDir.resolve("output.md").toFile();
        try (InputStream is = classpathStream("sample1.xls")) {
            Excel2MarkdownUtils.xls2MarkdownFile(is, target);
        }

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertFalse(content.isBlank());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 格式一致性：xls 与 xlsx 输出结构相同
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xls2Markdown_outputStructure_matchesXlsxStyle() throws Exception {
        // xls 输出同样遵循 Markdown 表格规范：每行以 | 开头，分隔行全为 ---
        try (InputStream is = classpathStream("sample1.xls")) {
            String result = Excel2MarkdownUtils.xls2Markdown(is);

            result.lines()
                    .filter(l -> l.startsWith("|") && l.contains("---"))
                    .forEach(sep -> assertTrue(sep.matches("\\|( --- \\|)+"),
                            "分隔行格式不正确: " + sep));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // xlsx 真实文件场景：美国卡派运费报价矩阵.xlsx
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void xlsx2Markdown_kaipaiMatrix_allSheetsPresent() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 5 个 sheet 全部输出为 # 一级标题
            assertTrue(result.contains("# 封面"),       "缺少 sheet：封面");
            assertTrue(result.contains("# 基础运费矩阵"), "缺少 sheet：基础运费矩阵");
            assertTrue(result.contains("# 区域划分表"),  "缺少 sheet：区域划分表");
            assertTrue(result.contains("# 附加费用"),    "缺少 sheet：附加费用");
            assertTrue(result.contains("# 计算示例"),    "缺少 sheet：计算示例");
            saveOutput(result, "美国卡派运费报价矩阵.xlsx.md");
        }
    }

    @Test
    void xlsx2Markdown_kaipaiMatrix_freightRateTableContent() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 运费价格矩阵：Zone 列头
            assertTrue(result.contains("Zone 2"), "应包含 Zone 2");
            assertTrue(result.contains("Zone 8"), "应包含 Zone 8");
            // 重量区间行
            assertTrue(result.contains("1-5 lbs"),    "应包含最小重量段 1-5 lbs");
            assertTrue(result.contains("1000+ lbs"),  "应包含最大重量段 1000+ lbs");
            // 具体费率（Zone 2 / 1-5 lbs / Ground）
            assertTrue(result.contains("$8.50"),  "应包含费率 $8.50");
            assertTrue(result.contains("$15.20"), "应包含费率 $15.20");
        }
    }

    @Test
    void xlsx2Markdown_kaipaiMatrix_zoneTableContent() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 区域划分表：州简称 + 全称
            assertTrue(result.contains("California"), "应包含加州全名");
            assertTrue(result.contains("New York"),   "应包含纽约全名");
            assertTrue(result.contains("CA"),         "应包含加州简称 CA");
            assertTrue(result.contains("NY"),         "应包含纽约简称 NY");
        }
    }

    @Test
    void xlsx2Markdown_kaipaiMatrix_surchargeTableContent() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 附加费用表关键字段
            assertTrue(result.contains("Fuel Surcharge"), "应包含燃油附加费英文名");
            assertTrue(result.contains("$5.50"),          "应包含住宅派送费 $5.50");
            assertTrue(result.contains("$35.00"),         "应包含超大件附加费 $35.00");
        }
    }

    @Test
    void xlsx2Markdown_kaipaiMatrix_cellNewlineRenderedAsBr() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 运费价格表表头单元格含换行（如 "Zone 2\n(0-150mi)"），应渲染为 <br>
            assertTrue(result.contains("<br>"), "单元格内换行应渲染为 <br>");
            assertTrue(result.contains("0-150mi"), "Zone 2 里程说明应存在");
        }
    }

    @Test
    void xlsx2Markdown_kaipaiMatrix_structureCorrect() throws Exception {
        try (InputStream is = classpathStream("美国卡派运费报价矩阵.xlsx")) {
            String result = Excel2MarkdownUtils.xlsx2Markdown(is);

            // 新语义：每个 sheet 输出为一张完整表，表头为列字母（A, B, C, ...）
            assertTrue(result.contains("| A |"), "table header must use column letters");

            // 内容作为表格单元格值出现，不再是独立文本块
            assertTrue(result.contains("美国卡派运费报价矩阵"), "title content must appear in output");
            assertTrue(result.contains("基础运费矩阵"), "freight matrix content must appear in output");
            assertTrue(result.contains("Zone 2"), "zone labels must appear in output");

            // 不再有"单值行文本块 + 接续表格"的拆分模式
            assertFalse(result.contains("美国卡派运费报价矩阵\n\nUS Ground Freight Rate Matrix"),
                    "title must be a table cell, not a standalone text block");
            assertFalse(result.contains("报价说明\n\n|"),
                    "section heading must be a table cell, not a text block preceding a table");
        }
    }

    @Test
    void xlsx2MarkdownFile_kaipaiMatrix_writesUtf8File(@TempDir Path tmpDir) throws Exception {
        File source = classpathFile("美国卡派运费报价矩阵.xlsx");
        File target = tmpDir.resolve("美国卡派运费报价矩阵.md").toFile();

        Excel2MarkdownUtils.xlsx2MarkdownFile(source, target);

        assertTrue(target.exists());
        String content = Files.readString(target.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("基础运费矩阵"), "写出文件中文内容不应乱码");
        assertTrue(content.contains("Zone 2"),      "写出文件英文内容应存在");
    }
}
