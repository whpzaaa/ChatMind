package org.example.chatmind.service.impl;

import lombok.AllArgsConstructor;
import org.example.chatmind.agent.tools.Tool;
import org.example.chatmind.agent.tools.ToolType;
import org.example.chatmind.service.ToolService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final List<Tool> tools;
    @Override
    public List<Tool> getAllTools() {
        return tools;
    }

    @Override
    public List<Tool> getOptionalTools() {
        return  getToolByType(ToolType.OPTIONAL);
    }

    @Override
    public List<Tool> getFixedTools() {
        return getToolByType(ToolType.FIXED);
    }

    @Override
    public Tool getByName(String name) {
        Map<String, Tool> map = getOptionalTools().stream().collect(Collectors.toMap(Tool::getName, tool -> tool));
        return map.get(name);
    }

    private List<Tool> getToolByType(ToolType type) {
        return tools.stream().filter(tool -> tool.getType().equals(type)).toList();
    }
}
