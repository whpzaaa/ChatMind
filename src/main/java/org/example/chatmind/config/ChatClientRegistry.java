package org.example.chatmind.config;

import org.example.chatmind.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatClientRegistry {

    private final Map<String, ChatClient> chatClients;

    public ChatClientRegistry(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public ChatClient getChatClient(String chatClientName) {
        return chatClients.get(chatClientName);
    }
}
