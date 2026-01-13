package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.KnowledgeBaseDTO;
import org.example.chatmind.model.vo.KnowledgeBaseVO;
import org.example.chatmind.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public ApiResponse<String> create(@RequestBody KnowledgeBaseDTO dto) {
        String knowledgeBaseId = knowledgeBaseService.create(dto);
        return ApiResponse.success(knowledgeBaseId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody KnowledgeBaseDTO dto) {
        knowledgeBaseService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        knowledgeBaseService.delete(id);
        return ApiResponse.success();
    }

//    @GetMapping("/{id}")
//    public ApiResponse<KnowledgeBaseVO> getById(@PathVariable String id) {
//        KnowledgeBaseVO knowledgeBaseVO = knowledgeBaseService.getById(id);
//        return ApiResponse.success(knowledgeBaseVO);
//    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseVO>> getAll() {
        List<KnowledgeBaseVO> knowledgeBases = knowledgeBaseService.getAll();
        return ApiResponse.success(knowledgeBases);
    }

//    @GetMapping("/page")
//    public ApiResponse<List<KnowledgeBaseVO>> getByPage(@RequestParam(defaultValue = "1") int pageNum,
//                                                         @RequestParam(defaultValue = "10") int pageSize) {
//        List<KnowledgeBaseVO> knowledgeBases = knowledgeBaseService.getByPage(pageNum, pageSize);
//        return ApiResponse.success(knowledgeBases);
//    }
//
//    @GetMapping("/count")
//    public ApiResponse<Integer> count() {
//        int count = knowledgeBaseService.count();
//        return ApiResponse.success(count);
//    }
}
