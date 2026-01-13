package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.*;
import org.example.chatmind.model.entity.ChatMessage;

import java.util.List;

@Mapper
public interface ChatMessageMapper {


    int insert(ChatMessage chatMessage);


    int updateById(ChatMessage chatMessage);


    int deleteById(String id);


    ChatMessage selectById(String id);


    List<ChatMessage> selectBySessionId(String sessionId);

    List<ChatMessage> selectBySessionIdRecently(String sessionId, int limit);


    List<ChatMessage> selectAll();

}
