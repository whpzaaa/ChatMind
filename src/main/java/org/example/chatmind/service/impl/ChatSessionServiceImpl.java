package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.ChatSessionMapper;
import org.example.chatmind.model.dto.ChatSessionDTO;
import org.example.chatmind.model.entity.ChatSession;
import org.example.chatmind.model.vo.ChatSessionVO;
import org.example.chatmind.service.ChatSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String create(ChatSessionDTO dto) {
        ChatSession chatSession = convertToEntity(dto);
        chatSession.setCreatedAt(LocalDateTime.now());
        chatSession.setUpdatedAt(LocalDateTime.now());
        int result = chatSessionMapper.insert(chatSession);
        if(result <= 0){
            throw new BizException("创建chatSession失败");
        }
        return chatSession.getId();
    }

    @Override
    public ChatSessionVO update(String id, ChatSessionDTO dto) {
        ChatSession existingChatSession = chatSessionMapper.selectById(id);
        if (existingChatSession == null) {
            throw new RuntimeException("ChatSession not found with id: " + id);
        }

        ChatSession chatSession = convertToEntity(dto);
        chatSession.setId(id);
        chatSession.setCreatedAt(existingChatSession.getCreatedAt());
        chatSession.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(chatSession);
        return convertToVO(chatSession);
    }

    @Override
    public void delete(String id) {
        chatSessionMapper.deleteById(id);
    }

    @Override
    public ChatSessionVO getById(String id) {
        ChatSession chatSession = chatSessionMapper.selectById(id);
        if (chatSession == null) {
            throw new RuntimeException("ChatSession not found with id: " + id);
        }
        return convertToVO(chatSession);
    }

    @Override
    public List<ChatSessionVO> getByAgentId(String agentId) {
        List<ChatSession> chatSessions = chatSessionMapper.selectByAgentId(agentId);
        return chatSessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionVO> getAll() {
        List<ChatSession> chatSessions = chatSessionMapper.selectAll();
        return chatSessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionVO> getByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<ChatSession> chatSessions = chatSessionMapper.selectByPage(offset, pageSize);
        return chatSessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public int count() {
        return chatSessionMapper.count();
    }

    @Override
    public int countByAgentId(String agentId) {
        return chatSessionMapper.countByAgentId(agentId);
    }

    private ChatSession convertToEntity(ChatSessionDTO dto) {
        ChatSession chatSession = new ChatSession();
        BeanUtils.copyProperties(dto, chatSession, "metadata");

        try {
            if (dto.getMetadata() != null) {
                chatSession.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        return chatSession;
    }

    private ChatSessionVO convertToVO(ChatSession chatSession) {
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(chatSession, vo);
        return vo;
    }
}
