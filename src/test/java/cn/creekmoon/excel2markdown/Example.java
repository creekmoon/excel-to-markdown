package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Example {

    public static void main(String[] args) {
        String sourceFileName = "报价2.xlsx";
        try {
            URL url = Example.class.getClassLoader().getResource(sourceFileName);
            if (url == null) {
                throw new IllegalStateException("classpath 中找不到资源: " + sourceFileName);
            }
            File source = new File(url.toURI());

            Path targetDir = Paths.get("target");
            Files.createDirectories(targetDir);
            String mdName = sourceFileName.replaceFirst("(?i)\\.(xlsx|xls|csv)$", ".md");
            File target = targetDir.resolve(mdName).toFile();

            String lower = sourceFileName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".xlsx")) {
                Excel2MarkdownUtils.xlsx2MarkdownFile(source, target);
            } else if (lower.endsWith(".xls")) {
                Excel2MarkdownUtils.xls2MarkdownFile(source, target);
            } else if (lower.endsWith(".csv")) {
                Excel2MarkdownUtils.csv2MarkdownFile(source, target);
            } else {
                throw new IllegalArgumentException("不支持的文件类型，仅支持 .xlsx、.xls、.csv: " + sourceFileName);
            }
            System.out.println("已生成 Markdown: " + target.getAbsolutePath());
        } catch (Excel2MarkdownException e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (URISyntaxException e) {
            System.err.println("解析资源路径失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
