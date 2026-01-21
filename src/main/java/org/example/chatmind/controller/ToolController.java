package org.example.chatmind.controller;

import lombok.AllArgsConstructor;
import org.example.chatmind.agent.tools.Tool;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.service.impl.ToolServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ToolController {

    private final ToolServiceImpl toolService;

    @RequestMapping("/tools")
    public ApiResponse<List<Tool>> getTools() {
        return ApiResponse.success(toolService.getOptionalTools());
    }
}
