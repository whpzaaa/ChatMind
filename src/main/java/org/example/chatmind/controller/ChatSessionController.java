package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.ChatSessionDTO;
import org.example.chatmind.model.request.CreateChatSessionRequest;
import org.example.chatmind.model.request.UpdateChatSessionRequest;
import org.example.chatmind.model.response.CreateChatSessionResponse;
import org.example.chatmind.model.response.GetChatMessagesResponse;
import org.example.chatmind.model.response.GetChatSessionResponse;
import org.example.chatmind.model.response.GetChatSessionsResponse;
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
    public ApiResponse<CreateChatSessionResponse> create(@RequestBody CreateChatSessionRequest request) {
        ChatSessionDTO dto = convertToDTO(request);
        String chatSessionId = chatSessionService.create(dto);
        return ApiResponse.success(CreateChatSessionResponse.builder().chatSessionId(chatSessionId).build());
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody UpdateChatSessionRequest request) {
        ChatSessionDTO dto = convertToDTO(request);
        chatSessionService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        chatSessionService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<GetChatSessionResponse> getById(@PathVariable String id) {
        ChatSessionVO chatSessionVO = chatSessionService.getById(id);
        return ApiResponse.success(GetChatSessionResponse.builder().chatSession(chatSessionVO).build());
    }

    @GetMapping("/agent/{agentId}")
    public ApiResponse<GetChatSessionsResponse> getByAgentId(@PathVariable String agentId) {
        List<ChatSessionVO> chatSessions = chatSessionService.getByAgentId(agentId);
        return ApiResponse.success(GetChatSessionsResponse.builder().chatSessions(chatSessions.toArray(new ChatSessionVO[0])).build());
    }

    @GetMapping
    public ApiResponse<GetChatSessionsResponse> getAll() {
        List<ChatSessionVO> chatSessions = chatSessionService.getAll();
        return ApiResponse.success(GetChatSessionsResponse.builder().chatSessions(chatSessions.toArray(new ChatSessionVO[0])).build());
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

    private ChatSessionDTO convertToDTO(CreateChatSessionRequest request) {
        return ChatSessionDTO.builder()
                .agentId(request.getAgentId())
                .title(request.getTitle())
                .build();
    }

    private ChatSessionDTO convertToDTO(UpdateChatSessionRequest request) {
        return ChatSessionDTO.builder()
                .title(request.getTitle())
                .build();
    }
}
