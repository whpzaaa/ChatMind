package org.example.chatmind.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatmind.model.dto.ChatMessageDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {
    private String id;
    private String sessionId;
    private ChatMessageDTO.RoleType role;
    private String content;
    private ChatMessageDTO.MetaData metadata;
}
