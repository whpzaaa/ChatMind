package org.example.chatmind.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.example.chatmind.agent.ChatMind;
import org.example.chatmind.agent.ChatMindFactory;
import org.example.chatmind.event.ChatEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {
    private final ChatMindFactory chatMindFactory;

    @Async
    @EventListener
    public void handle(ChatEvent chatEvent) throws JsonProcessingException {
        ChatMind chatMind = chatMindFactory.createChatMind(chatEvent.getAgentId(), chatEvent.getChatSessionId());
        chatMind.run();
    }
}
