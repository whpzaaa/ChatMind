package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.AgentMapper;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.AgentDTO;
import org.example.chatmind.model.entity.Agent;
import org.example.chatmind.model.vo.AgentVO;
import org.example.chatmind.service.AgentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentMapper agentMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String create(AgentDTO dto) {
        Agent agent = convertToEntity(dto);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        int count = agentMapper.insert(agent);
        if(count <= 0){
            throw new BizException("创建agent失败");
        }
        String agentId = agent.getId();
        return agentId;
    }

    @Override
    public void update(String id, AgentDTO dto) {
        Agent existingAgent = agentMapper.selectById(id);
        if (existingAgent == null) {
            throw new RuntimeException("Agent not found with id: " + id);
        }

        Agent agent = convertToEntity(dto);
        agent.setId(id);
        agent.setCreatedAt(existingAgent.getCreatedAt());
        agent.setUpdatedAt(LocalDateTime.now());
        agentMapper.updateById(agent);
    }

    @Override
    public void delete(String id) {
        agentMapper.deleteById(id);
    }

//    @Override
//    public AgentVO getById(String id) {
//        Agent agent = agentMapper.selectById(id);
//        if (agent == null) {
//            throw new RuntimeException("Agent not found with id: " + id);
//        }
//        return convertToVO(agent);
//    }

    @Override
    public List<AgentVO> getAll() {
        List<Agent> agents = agentMapper.selectAll();
        return agents.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }


    private Agent convertToEntity(AgentDTO dto) {
        Agent agent = new Agent();
        BeanUtils.copyProperties(dto, agent,"model", "allowedTools", "allowedKbs", "chatOptions");

        try {
            if (dto.getModel() != null){
                agent.setModel(dto.getModel().getModelName());
            }
            if (dto.getAllowedTools() != null) {
                agent.setAllowedTools(objectMapper.writeValueAsString(dto.getAllowedTools()));
            }
            if (dto.getAllowedKbs() != null) {
                agent.setAllowedKbs(objectMapper.writeValueAsString(dto.getAllowedKbs()));
            }
            if (dto.getChatOptions() != null) {
                agent.setChatOptions(objectMapper.writeValueAsString(dto.getChatOptions()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        return agent;
    }

    private AgentVO convertToVO(Agent agent) {
        AgentVO vo = new AgentVO();
        BeanUtils.copyProperties(agent, vo, "model","allowedTools", "allowedKbs", "chatOptions");

        try {
            if (agent.getModel() != null){
                vo.setModel(AgentDTO.ModelType.fromModelName(agent.getModel()));
            }
            if (agent.getAllowedTools() != null) {
                vo.setAllowedTools(objectMapper.readValue(agent.getAllowedTools(), new TypeReference<List<String>>() {}));
            }
            if (agent.getAllowedKbs() != null) {
                vo.setAllowedKbs(objectMapper.readValue(agent.getAllowedKbs(), new TypeReference<List<String>>() {}));
            }
            if (agent.getChatOptions() != null) {
                vo.setChatOptions(objectMapper.readValue(agent.getChatOptions(), AgentDTO.ChatOptions.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }

        return vo;
    }
}
