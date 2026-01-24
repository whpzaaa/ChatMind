package org.example.chatmind.model.response;


import lombok.Builder;
import lombok.Data;
import org.example.chatmind.model.vo.ChatMessageVO;

@Data
@Builder
public class GetChatMessagesResponse {
    private ChatMessageVO[] chatMessages;
}

