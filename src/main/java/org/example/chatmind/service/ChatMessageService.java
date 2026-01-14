package org.example.chatmind.service;

import org.example.chatmind.model.dto.ChatMessageDTO;
import org.example.chatmind.model.vo.ChatMessageVO;

import java.util.List;

public interface ChatMessageService {

    String create(ChatMessageDTO dto);

    void update(String id, ChatMessageDTO dto);

    void delete(String id);

    List<ChatMessageVO> getBySessionId(String sessionId);

    List<ChatMessageVO> getBySessionIdRecently(String sessionId,int limit);

}
