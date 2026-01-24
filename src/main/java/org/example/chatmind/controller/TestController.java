package org.example.chatmind.controller;


import lombok.AllArgsConstructor;
import org.example.chatmind.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {

    private final SseService sseService;

    @RequestMapping("/health")
    public String health() {
        return "ok";
    }

    @GetMapping("/sse-test")
    public String sseTest() {
        return "ok";
    }
}
