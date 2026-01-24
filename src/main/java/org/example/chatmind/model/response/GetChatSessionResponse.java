package org.example.chatmind.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.chatmind.model.vo.ChatSessionVO;

@Data
@AllArgsConstructor
@Builder
public class GetChatSessionResponse {
    private ChatSessionVO chatSession;
}
