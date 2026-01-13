package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.AgentDTO;
import org.example.chatmind.model.vo.AgentVO;
import org.example.chatmind.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    public ApiResponse<String> create(@RequestBody AgentDTO dto) {
        String agentId = agentService.create(dto);
        return ApiResponse.success(agentId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody AgentDTO dto) {
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
    public ApiResponse<List<AgentVO>> getAll() {
        List<AgentVO> agents = agentService.getAll();
        return ApiResponse.success(agents);
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
}
