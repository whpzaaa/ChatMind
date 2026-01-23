package org.example.chatmind.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Markdown 解析服务
 */
public interface MarkdownParserService {


    /**
     * 解析 markdown 提取标题和对应的内容
     *
     * @param inputStream Markdown 文件输入流
     * @return 标题和内容的列表，每个元素包含标题和该标题下的内容
     */
    List<MarkdownSection> parseMarkdown(InputStream inputStream) throws IOException;
    /**
     * Markdown 段落结构
     */
    @Data
    @AllArgsConstructor
    @ToString
    class MarkdownSection {
        private String title;
        private String content;
    }
}
