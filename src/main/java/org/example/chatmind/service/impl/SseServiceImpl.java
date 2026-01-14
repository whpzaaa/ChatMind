package org.example.chatmind.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.chatmind.model.common.SseMessage;
import org.example.chatmind.service.SseService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class SseServiceImpl implements SseService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    public SseEmitter connect(String chatSessionId) {
        SseEmitter sseEmitter = new SseEmitter(30*60*1000L);
        emitters.put(chatSessionId, sseEmitter);
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sseEmitter.onError(throwable -> emitters.remove(chatSessionId));
        sseEmitter.onCompletion(() -> emitters.remove(chatSessionId));
        sseEmitter.onTimeout(() -> emitters.remove(chatSessionId));
        return sseEmitter;
    }

    @Override
    public void sendMessage(String chatSessionId, SseMessage message) {
        SseEmitter sseEmitter = emitters.get(chatSessionId);
        if (sseEmitter != null) {
            try {

                sseEmitter.send(SseEmitter.event()
                        .name("message")
                        .data(objectMapper.writeValueAsString( message)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            throw new RuntimeException("No emitter found for chat session: " + chatSessionId);
        }
    }
}
