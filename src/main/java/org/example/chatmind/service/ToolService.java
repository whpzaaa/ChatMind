package org.example.chatmind.service;

import org.example.chatmind.agent.tools.Tool;

import java.util.List;

public interface ToolService {
    List<Tool> getAllTools();

    List<Tool> getOptionalTools();

    List<Tool> getFixedTools();

    Tool getByName(String name);

}
