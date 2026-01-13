package org.example.chatmind.service;

import org.example.chatmind.model.dto.ChatSessionDTO;
import org.example.chatmind.model.vo.ChatSessionVO;

import java.util.List;

public interface ChatSessionService {

    String create(ChatSessionDTO dto);

    ChatSessionVO update(String id, ChatSessionDTO dto);

    void delete(String id);

    ChatSessionVO getById(String id);

    List<ChatSessionVO> getByAgentId(String agentId);

    List<ChatSessionVO> getAll();

    List<ChatSessionVO> getByPage(int pageNum, int pageSize);

    int count();

    int countByAgentId(String agentId);
}
