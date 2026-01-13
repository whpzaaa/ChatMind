package org.example.chatmind.service;

import org.example.chatmind.model.dto.AgentDTO;
import org.example.chatmind.model.vo.AgentVO;

import java.util.List;

public interface AgentService {

    String create(AgentDTO dto);

    void update(String id, AgentDTO dto);

    void delete(String id);

//    AgentVO getById(String id);

    List<AgentVO> getAll();

}
