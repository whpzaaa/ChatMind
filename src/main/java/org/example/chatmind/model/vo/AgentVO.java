package org.example.chatmind.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatmind.model.dto.AgentDTO;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentVO {
    private String id;

    private String name;

    private String description;

    private String systemPrompt;

    private AgentDTO.ModelType model;

    private List<String> allowedTools;

    private List<String> allowedKbs;

    private AgentDTO.ChatOptions chatOptions;
}
