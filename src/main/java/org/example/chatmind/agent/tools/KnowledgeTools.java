package org.example.chatmind.agent.tools;

import org.example.chatmind.service.RagService;

import java.util.List;

public class KnowledgeTools implements  Tool{
    @Override
    public String getName() {
        return "KnowledgeTool";
    }

    @Override
    public String getDescription() {
        return "用于从知识库执行语义检索（RAG）。输入知识库 ID 和查询文本，返回与查询最相关的内容片段。";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    private final RagService ragService;
    public KnowledgeTools(RagService ragService) {
        this.ragService = ragService;
    }

    @org.springframework.ai.tool.annotation.Tool(
            name = "KnowledgeTool",
            description = "从指定知识库中执行相似性检索（RAG）。参数为知识库 ID（kbsId）和查询文本（query），返回与查询最相关的知识片段。"
    )
    public String knowledgeQuery(String kbsId, String query) {
        List<String> results = ragService.search(kbsId, query);
        return String.join("\n", results);
    }
}
