package org.example.chatmind.service;

import org.example.chatmind.model.common.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter connect(String chatSessionId);

    void sendMessage(String chatSessionId, SseMessage message);
}
