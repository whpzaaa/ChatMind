package org.example.chatmind.agent.tools.test;


import org.example.chatmind.agent.tools.Tool;
import org.example.chatmind.agent.tools.ToolType;
import org.springframework.stereotype.Component;

@Component
public class CityTool implements Tool {
    @Override
    public String getName() {
        return "cityTool";
    }

    @Override
    public String getDescription() {
        return "获取当前的城市";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "getCity", description = "获取当前的城市")
    public String getCity() {
        return "深圳";
    }
}
