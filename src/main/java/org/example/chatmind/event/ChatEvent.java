package org.example.chatmind.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatEvent {
    private String agentId;
    private String chatSessionId;
    private String userInput;
}
