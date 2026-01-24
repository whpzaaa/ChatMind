package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.ChatMessageDTO;
import org.example.chatmind.model.response.CreateChatMessageResponse;
import org.example.chatmind.model.response.GetChatMessagesResponse;
import org.example.chatmind.model.vo.ChatMessageVO;
import org.example.chatmind.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    public ApiResponse<CreateChatMessageResponse> create(@RequestBody ChatMessageDTO dto) {
        String chatMessageId = chatMessageService.create(dto);
        return ApiResponse.success(CreateChatMessageResponse.builder().chatMessageId(chatMessageId).build());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody ChatMessageDTO dto) {
        chatMessageService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        chatMessageService.delete(id);
        return ApiResponse.success();
    }



    @GetMapping("/session/{sessionId}")
    public ApiResponse<GetChatMessagesResponse> getBySessionId(@PathVariable String sessionId) {
        List<ChatMessageVO> chatMessages = chatMessageService.getBySessionId(sessionId);
        return ApiResponse.success(GetChatMessagesResponse.builder().chatMessages(chatMessages.toArray(new ChatMessageVO[0])).build());
    }

    //    @GetMapping("/{id}")
//    public ApiResponse<ChatMessageVO> getById(@PathVariable String id) {
//        ChatMessageVO chatMessageVO = chatMessageService.getById(id);
//        return ApiResponse.success(chatMessageVO);
//    }

//    @GetMapping
//    public ApiResponse<List<ChatMessageVO>> getAll() {
//        List<ChatMessageVO> chatMessages = chatMessageService.getAll();
//        return ApiResponse.success(chatMessages);
//    }
//
//
//    @GetMapping("/session/{sessionId}/count")
//    public ApiResponse<Integer> countBySessionId(@PathVariable String sessionId) {
//        int count = chatMessageService.countBySessionId(sessionId);
//        return ApiResponse.success(count);
//    }
}
