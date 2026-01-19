package org.example.chatmind.agent;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.config.ChatClientRegistry;
import org.example.chatmind.mapper.AgentMapper;
import org.example.chatmind.mapper.ChatMessageMapper;
import org.example.chatmind.mapper.KnowledgeBaseMapper;
import org.example.chatmind.model.dto.AgentDTO;
import org.example.chatmind.model.dto.ChatMessageDTO;
import org.example.chatmind.model.dto.KnowledgeBaseDTO;
import org.example.chatmind.model.entity.Agent;
import org.example.chatmind.model.entity.ChatMessage;
import org.example.chatmind.model.entity.KnowledgeBase;
import org.example.chatmind.model.vo.AgentVO;
import org.example.chatmind.service.ChatMessageService;
import org.example.chatmind.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ChatMindFactory {
    private final ChatClientRegistry chatClientRegistry;
    private final SseService sseService;
    private final AgentMapper agentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ChatMessageMapper  chatMessageMapper;
    private final ObjectMapper objectMapper;
    private  AgentVO agentConfig;
    private final ChatMessageService chatMessageService;

    public ChatMindFactory(ChatClientRegistry chatClientRegistry, SseService sseService, AgentMapper agentMapper, KnowledgeBaseMapper knowledgeBaseMapper, ChatMessageMapper chatMessageMapper, ObjectMapper objectMapper, ChatMessageService chatMessageService) {
        this.chatClientRegistry = chatClientRegistry;
        this.sseService = sseService;
        this.agentMapper = agentMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.objectMapper = objectMapper;
        this.chatMessageService = chatMessageService;
    }
    private Agent getAgent(String agentId) {
        return agentMapper.selectById(agentId);
    }

    //将数据中的记忆恢复成list<message>结构
    private List<Message> restoreMemory(String chatSessionId) throws JsonProcessingException {
        List<Message> memory = new ArrayList<>();
        Integer messageLength = agentConfig.getChatOptions().getMessageLength();
        List<ChatMessage> chatMessages = chatMessageMapper.selectBySessionIdRecently(chatSessionId, messageLength);
        List<ChatMessageDTO> chatMessageDTOS = convertToMessageDTO(chatMessages);
        for (ChatMessageDTO chatMessageDTO : chatMessageDTOS){
            switch (chatMessageDTO.getRole()){
                case ASSISTANT :
                    memory.add(AssistantMessage.builder()
                        .content(chatMessageDTO.getContent())
                        .toolCalls(chatMessageDTO.getMetadata().getToolCalls())
                        .build());
                    break;
                case TOOL  :
                    memory.add(ToolResponseMessage.builder()
                          .responses(List.of(chatMessageDTO.getMetadata().getToolResponse()))
                        .build());
                    break;
                case SYSTEM :
                    if(!StringUtils.hasLength(chatMessageDTO.getContent())) continue;
                    memory.add(0,new SystemMessage(chatMessageDTO.getContent()));
                    break;
                case USER :
                    if (!StringUtils.hasLength(chatMessageDTO.getContent())) continue;
                    memory.add(new UserMessage(chatMessageDTO.getContent()));
                    break;
                default:
                    log.error("unknown role: {},content = {}", chatMessageDTO.getRole(),chatMessageDTO.getContent());
                    throw new IllegalArgumentException("unknown role");
            }
        }
        return memory;
    }

    private List<ChatMessageDTO> convertToMessageDTO(List<ChatMessage> chatMessages) throws JsonProcessingException {
        List<ChatMessageDTO> dtos = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages){
            ChatMessageDTO dto = ChatMessageDTO.builder()
                    .id(chatMessage.getId())
                    .sessionId(chatMessage.getSessionId())
                    .role(ChatMessageDTO.RoleType.valueOf(chatMessage.getRole()))
                    .content(chatMessage.getContent())
                    .metadata(objectMapper.readValue(chatMessage.getMetadata(), ChatMessageDTO.MetaData.class))
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    //把agent转换agentvo
    private AgentVO convertToVO(Agent agent)  {
        try {
            BeanUtils.copyProperties(agent, agentConfig, "allowedTools", "allowedKbs", "chatOptions");
            agentConfig.setChatOptions(objectMapper.readValue(agent.getChatOptions(), AgentDTO.ChatOptions.class));
            agentConfig.setAllowedTools(objectMapper.readValue(agent.getAllowedTools(), new TypeReference<List<String>>() {}));
            agentConfig.setAllowedKbs(objectMapper.readValue(agent.getAllowedKbs(), new TypeReference<List<String>>() {}));
            return agentConfig;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("agent json parse error");
        }
    }


    //获取knowledgebases
    private List<KnowledgeBaseDTO> getKnowledgeBases(AgentVO agentConfig) {
        List<String> allowedKbIds = agentConfig.getAllowedKbs();
        if(allowedKbIds == null || allowedKbIds.isEmpty()) return Collections.emptyList();
        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectByIdBatch(agentConfig.getAllowedKbs());
        if(knowledgeBases.isEmpty()) return Collections.emptyList();
        return knowledgeBases.stream()
                .map(knowledgeBase -> {
                    try {
                        return KnowledgeBaseDTO.builder()
                                .id(knowledgeBase.getId())
                                .name(knowledgeBase.getName())
                                .description(knowledgeBase.getDescription())
                                .metadata(objectMapper.readValue(knowledgeBase.getMetadata(), KnowledgeBaseDTO.MetaData.class))
                                .createdAt(knowledgeBase.getCreatedAt())
                                .updatedAt(knowledgeBase.getUpdatedAt())
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    //TODO:实现获取tools

    private ChatMind buildChatMind(
            Agent agent,
            List<Message> memory,
            List<ToolCallback> toolCallbacks,
            List<KnowledgeBaseDTO> availableKnowledgeBases,
            String chatSessionId
    ) {
        ChatClient chatClient = chatClientRegistry.getChatClient(agent.getModel());
        if(chatClient == null) throw new IllegalArgumentException("chat client not found");
        return new ChatMind(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getSystemPrompt(),
                chatClient,
                agentConfig.getChatOptions().getMessageLength(),
                memory,
                toolCallbacks,
                availableKnowledgeBases,
                chatSessionId,
                sseService,
                chatMessageService
        );
    }

    //创建chatmind实例 对外方法
    public ChatMind createChatMind(String agentId, String chatSessionId) throws JsonProcessingException {
        Agent agent = getAgent(agentId);
        AgentVO agentConfig = convertToVO(agent);
        List<Message> memory = restoreMemory(chatSessionId);
        List<KnowledgeBaseDTO> availableKnowledgeBases = getKnowledgeBases(agentConfig);
        return buildChatMind(
                agent,
                memory,
                null,
                availableKnowledgeBases,
                chatSessionId);
    }
}
