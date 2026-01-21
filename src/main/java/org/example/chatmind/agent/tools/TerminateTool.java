package org.example.chatmind.agent.tools;

import org.springframework.stereotype.Component;

@Component
public class TerminateTool implements Tool {
    @Override
    public String getName() {
        return "terminate";
    }

    @Override
    public String getDescription() {
        return "跳出 Agent Loop 的终止工具";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "terminate", description = "跳出 Agent Loop 的终止工具")
    public void terminate() {}
}
