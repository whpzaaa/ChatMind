package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.AgentDTO;
import org.example.chatmind.model.request.CreateAgentRequest;
import org.example.chatmind.model.request.UpdateAgentRequest;
import org.example.chatmind.model.response.CreateAgentResponse;
import org.example.chatmind.model.response.GetAgentsResponse;
import org.example.chatmind.model.vo.AgentVO;
import org.example.chatmind.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    public ApiResponse<CreateAgentResponse> create(@RequestBody CreateAgentRequest request) {
        AgentDTO dto = convertToDTO(request);
        String agentId = agentService.create(dto);
        return ApiResponse.success(CreateAgentResponse.builder().agentId(agentId).build());
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody UpdateAgentRequest request) {
        AgentDTO dto = convertToDTO(request);
        agentService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        agentService.delete(id);
        return ApiResponse.success();
    }

//    @GetMapping("/{id}")
//    public ApiResponse<AgentVO> getById(@PathVariable String id) {
//        AgentVO agentVO = agentService.getById(id);
//        return ApiResponse.success(agentVO);
//    }

    @GetMapping
    public ApiResponse<GetAgentsResponse> getAll() {
        List<AgentVO> agents = agentService.getAll();
        return ApiResponse.success(GetAgentsResponse.builder().agents(agents.toArray(new AgentVO[0])).build());
    }
//
//    @GetMapping("/page")
//    public ApiResponse<List<AgentVO>> getByPage(@RequestParam(defaultValue = "1") int pageNum,
//                                                 @RequestParam(defaultValue = "10") int pageSize) {
//        List<AgentVO> agents = agentService.getByPage(pageNum, pageSize);
//        return ApiResponse.success(agents);
//    }
//
//    @GetMapping("/count")
//    public ApiResponse<Integer> count() {
//        int count = agentService.count();
//        return ApiResponse.success(count);
//    }

    private AgentDTO convertToDTO(CreateAgentRequest request) {
        return AgentDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .systemPrompt(request.getSystemPrompt())
                .model(AgentDTO.ModelType.fromModelName(request.getModel()))
                .allowedTools(request.getAllowedTools())
                .allowedKbs(request.getAllowedKbs())
                .chatOptions(request.getChatOptions())
                .build();
    }

    private AgentDTO convertToDTO(UpdateAgentRequest request) {
        return AgentDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .systemPrompt(request.getSystemPrompt())
                .model(AgentDTO.ModelType.fromModelName(request.getModel()))
                .allowedTools(request.getAllowedTools())
                .allowedKbs(request.getAllowedKbs())
                .chatOptions(request.getChatOptions())
                .build();
    }
}
