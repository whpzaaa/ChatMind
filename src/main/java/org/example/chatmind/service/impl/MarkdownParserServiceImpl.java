package org.example.chatmind.service.impl;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.service.MarkdownParserService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class MarkdownParserServiceImpl implements MarkdownParserService {


    private final Parser parser;
    private String originalMarkdownContent;

    public MarkdownParserServiceImpl() {
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
    }

    @Override
    public List<MarkdownSection> parseMarkdown(InputStream inputStream) {
        try {
            //读取文件内容
            originalMarkdownContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            //解析markdown
            Document document = parser.parse(originalMarkdownContent);

            //提取标题和内容
            List<MarkdownSection> sections = new ArrayList<>();
            extractSections(document, sections);

            log.info("解析 Markdown 完成，共提取 {} 个章节", sections.size());
            return sections;
        } catch (IOException e) {
            log.error("markdown解析失败", e);
            throw new RuntimeException("markdown解析失败", e);
        }
    }

    /**
     * 提取标题和内容
     * 只遍历文档的直接子节点，遇到任何标题就停止收集当前标题的内容
     */
    private void extractSections(Document document, List<MarkdownSection> sections) {
        //收集文档的所有直接子节点（顶层节点）
        List<Node> topLevelNodes = new ArrayList<>();
        Node child = document.getFirstChild();
        while (child != null) {
            topLevelNodes.add(child);
            child = child.getNext();
        }

        //遍历所有顶层节点
        for(int i = 0; i < topLevelNodes.size(); i++){
            Node node = topLevelNodes.get(i);
            if(node instanceof Heading){
                Heading heading = (Heading) node;
                String title = extractHeadingText(heading);
                if(title == null || title.trim().isEmpty()) continue;

                // 收集当前标题到下一个标题（任何级别）之间的所有内容
                StringBuilder contentBuilder = new StringBuilder();
                for(int j = i + 1; j < topLevelNodes.size(); j++){
                    Node nextNode = topLevelNodes.get(j);
                    if(nextNode instanceof Heading) break;

                    String content = extractNodeContent(nextNode);
                    if(content != null && !content.trim().isEmpty()){
                        if(contentBuilder.length() > 0){
                            contentBuilder.append("\n");
                        }
                        contentBuilder.append(content);
                    }
                }
                String content = contentBuilder.toString().trim();
                sections.add(new MarkdownSection(title, content));
            }
        }
    }

    /**
     * 提取标题文本
     */
    private String extractHeadingText(Heading heading) {
        StringBuilder text  = new StringBuilder();
        Node child = heading.getFirstChild();
        while (child != null){
            String childText = extractPlainText(child);
            if(childText != null && !childText.trim().isEmpty()){
                if(text.length() > 0){
                    text.append(" ");
                }
                text.append(childText);
            }
            child = child.getNext();
        }
        return text.toString().trim();
    }

    /**
     * 提取节点内容（保留格式，特别是表格）
     */
    private String extractNodeContent(Node node) {
        if(node ==  null) return null;

        // 对于表格，保留原始 Markdown 格式
        if(node instanceof TableBlock){
            return extractTableMarkdown(node);
        }

        return extractPlainText(node);
    }


    /**
     * 提取表格的原始 Markdown 格式
     */
    private String extractTableMarkdown(Node node) {
        if(originalMarkdownContent == null){
            return extractPlainText(node);
        }

        try {
            BasedSequence chars = node.getChars();
            if(chars != null && chars.length() > 0){
                int startOffset = chars.getStartOffset();
                int endOffset = chars.getEndOffset();

                if(startOffset >= 0 && endOffset <= originalMarkdownContent.length()){
                    return originalMarkdownContent.substring(startOffset, endOffset).trim();
                }
            }
            return extractPlainText(node);
        } catch (Exception e) {
            log.warn("提取表格 Markdown 失败，使用文本提取: {}", e.getMessage());
            return extractPlainText(node);
        }
    }

    /**
     * 提取节点的纯文本内容
     */
    private String extractPlainText(Node node) {
        if(node == null){
            return null;
        }
        StringBuilder text = new StringBuilder();
        extractTextRecursive(node, text);
        return text.toString().trim();
    }

    /**
     * 递归提取文本
     */
    private void extractTextRecursive(Node node, StringBuilder text) {
        if(node == null){
            return;
        }
        // 跳过标题节点（标题已经在 extractSections 中单独处理）
        if(node instanceof Heading){
            return;
        }

        // 对于有子节点的节点，递归处理子节点
        Node child = node.getFirstChild();
        if(child != null){
            boolean isFirstChild = true;
            while (child != null){
                if(!isFirstChild && text.length() > 0){
                    if(child instanceof Block){
                        if(!text.toString().endsWith("\n")){
                            text.append("\n");
                        }
                    }else{
                        text.append(" ");
                    }
                }
                extractTextRecursive(child, text);
                child = child.getNext();
                isFirstChild = false;
            }
        }else {
            BasedSequence chars = node.getChars();
            if(chars != null && chars.length() > 0){
                String nodeText = chars.toString().trim();
                if(!nodeText.isEmpty()){
                    if(text.length() > 0 && !text.toString().endsWith("\n")){
                        text.append(" ");
                    }
                    text.append(nodeText);
                }
            }
        }

    }
}
