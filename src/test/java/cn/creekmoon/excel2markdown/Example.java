package cn.creekmoon.excel2markdown;

import cn.creekmoon.excel2markdown.exception.Excel2MarkdownException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Example {

    public static void main(String[] args) {
        String sourceFileName = "收集卡派价模板.xlsx";
        try {
            URL url = Example.class.getClassLoader().getResource(sourceFileName);
            if (url == null) {
                throw new IllegalStateException("classpath 中找不到资源: " + sourceFileName);
            }
            File source = new File(url.toURI());

            Path targetDir = Paths.get("target");
            Files.createDirectories(targetDir);
            String mdName = sourceFileName.replaceFirst("(?i)\\.xlsx", ".md");
            File target = targetDir.resolve(mdName).toFile();

            Excel2MarkdownUtils.xlsx2MarkdownFile(source, target);
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
