package org.example.chatmind.model.response;

import lombok.Builder;
import lombok.Data;
import org.example.chatmind.model.vo.ChatSessionVO;

@Data
@Builder
public class GetChatSessionsResponse {
    private ChatSessionVO[] chatSessions;
}
