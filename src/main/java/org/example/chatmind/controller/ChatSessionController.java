package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.ChatSessionDTO;
import org.example.chatmind.model.vo.ChatSessionVO;
import org.example.chatmind.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ApiResponse<String> create(@RequestBody ChatSessionDTO dto) {
        String chatSessionId = chatSessionService.create(dto);
        return ApiResponse.success(chatSessionId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody ChatSessionDTO dto) {
        chatSessionService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        chatSessionService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatSessionVO> getById(@PathVariable String id) {
        ChatSessionVO chatSessionVO = chatSessionService.getById(id);
        return ApiResponse.success(chatSessionVO);
    }

    @GetMapping("/agent/{agentId}")
    public ApiResponse<List<ChatSessionVO>> getByAgentId(@PathVariable String agentId) {
        List<ChatSessionVO> chatSessions = chatSessionService.getByAgentId(agentId);
        return ApiResponse.success(chatSessions);
    }

    @GetMapping
    public ApiResponse<List<ChatSessionVO>> getAll() {
        List<ChatSessionVO> chatSessions = chatSessionService.getAll();
        return ApiResponse.success(chatSessions);
    }

//    @GetMapping("/page")
//    public ApiResponse<List<ChatSessionVO>> getByPage(@RequestParam(defaultValue = "1") int pageNum,
//                                                         @RequestParam(defaultValue = "10") int pageSize) {
//        List<ChatSessionVO> chatSessions = chatSessionService.getByPage(pageNum, pageSize);
//        return ApiResponse.success(chatSessions);
//    }
//
//    @GetMapping("/count")
//    public ApiResponse<Integer> count() {
//        int count = chatSessionService.count();
//        return ApiResponse.success(count);
//    }
//
//    @GetMapping("/agent/{agentId}/count")
//    public ApiResponse<Integer> countByAgentId(@PathVariable String agentId) {
//        int count = chatSessionService.countByAgentId(agentId);
//        return ApiResponse.success(count);
//    }
}
