package org.example.chatmind.controller;


import lombok.AllArgsConstructor;
import org.example.chatmind.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@AllArgsConstructor
public class SseController {
    private final SseService sseService;
    @RequestMapping(value = "/connect/{chatSessionId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String chatSessionId) {
        return sseService.connect(chatSessionId);
    }
}
