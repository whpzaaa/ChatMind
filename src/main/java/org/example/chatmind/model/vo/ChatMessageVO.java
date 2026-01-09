package org.example.chatmind.model.vo;


import lombok.Builder;
import lombok.Data;
import org.example.chatmind.model.dto.ChatMessageDTO;

@Data
@Builder
public class ChatMessageVO {
    private String id;
    private String sessionId;
    private ChatMessageDTO.RoleType role;
    private String content;
    private ChatMessageDTO.MetaData metadata;
}
