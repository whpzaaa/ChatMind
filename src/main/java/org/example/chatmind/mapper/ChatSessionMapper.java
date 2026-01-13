package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.*;
import org.example.chatmind.model.entity.ChatSession;

import java.util.List;

@Mapper
public interface ChatSessionMapper {


    int insert(ChatSession chatSession);


    int updateById(ChatSession chatSession);


    int deleteById(String id);


    ChatSession selectById(String id);


    List<ChatSession> selectByAgentId(String agentId);


    List<ChatSession> selectAll();

}
