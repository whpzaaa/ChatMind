package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.ChatMessageMapper;
import org.example.chatmind.model.dto.ChatMessageDTO;
import org.example.chatmind.model.entity.ChatMessage;
import org.example.chatmind.model.vo.ChatMessageVO;
import org.example.chatmind.service.ChatMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String create(ChatMessageDTO dto) {
        ChatMessage chatMessage = convertToEntity(dto);
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setUpdatedAt(LocalDateTime.now());
        int result = chatMessageMapper.insert(chatMessage);
        if(result <= 0){
            throw new BizException("创建chatMessage失败");
        }
        return chatMessage.getId();
    }

    @Override
    public void update(String id, ChatMessageDTO dto) {
        ChatMessage existingChatMessage = chatMessageMapper.selectById(id);
        if (existingChatMessage == null) {
            throw new RuntimeException("ChatMessage not found with id: " + id);
        }

        ChatMessage chatMessage = convertToEntity(dto);
        chatMessage.setId(id);
        chatMessage.setCreatedAt(existingChatMessage.getCreatedAt());
        chatMessage.setUpdatedAt(LocalDateTime.now());
        chatMessageMapper.updateById(chatMessage);
    }

    @Override
    public void delete(String id) {
        chatMessageMapper.deleteById(id);
    }

    @Override
    public List<ChatMessageVO> getBySessionId(String sessionId) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectBySessionId(sessionId);
        return chatMessages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }


    private ChatMessage convertToEntity(ChatMessageDTO dto) {
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(dto, chatMessage, "role", "metadata");

        if (dto.getRole() != null) {
            chatMessage.setRole(dto.getRole().getRole());
        }

        try {
            if (dto.getMetadata() != null) {
                chatMessage.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        return chatMessage;
    }

    private ChatMessageVO convertToVO(ChatMessage chatMessage) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(chatMessage, vo, "role", "metadata");

        if (chatMessage.getRole() != null) {
            vo.setRole(ChatMessageDTO.RoleType.fromRole(chatMessage.getRole()));
        }

        try {
            if (chatMessage.getMetadata() != null) {
                vo.setMetadata(objectMapper.readValue(chatMessage.getMetadata(), ChatMessageDTO.MetaData.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }

        return vo;
    }
}
