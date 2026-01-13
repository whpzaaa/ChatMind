package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.DocumentDTO;
import org.example.chatmind.model.vo.DocumentVO;
import org.example.chatmind.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ApiResponse<String> create(@RequestBody DocumentDTO dto) {
        String documentId = documentService.create(dto);
        return ApiResponse.success(documentId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody DocumentDTO dto) {
        documentService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        documentService.delete(id);
        return ApiResponse.success();
    }


    @GetMapping("/kb/{kbId}")
    public ApiResponse<List<DocumentVO>> getByKbId(@PathVariable String kbId) {
        List<DocumentVO> documents = documentService.getByKbId(kbId);
        return ApiResponse.success(documents);
    }

    @GetMapping
    public ApiResponse<List<DocumentVO>> getAll() {
        List<DocumentVO> documents = documentService.getAll();
        return ApiResponse.success(documents);
    }

    //TODO: 上传文档（上传文件并创建记录）
//    @GetMapping("/page")
//    public ApiResponse<List<DocumentVO>> getByPage(@RequestParam(defaultValue = "1") int pageNum,
//                                                     @RequestParam(defaultValue = "10") int pageSize) {
//        List<DocumentVO> documents = documentService.getByPage(pageNum, pageSize);
//        return ApiResponse.success(documents);
//    }
//
//    @GetMapping("/count")
//    public ApiResponse<Integer> count() {
//        int count = documentService.count();
//        return ApiResponse.success(count);
//    }
//
//    @GetMapping("/kb/{kbId}/count")
//    public ApiResponse<Integer> countByKbId(@PathVariable String kbId) {
//        int count = documentService.countByKbId(kbId);
//        return ApiResponse.success(count);
//    }
}
