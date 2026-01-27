package org.example.chatmind.controller;

import lombok.RequiredArgsConstructor;
import org.example.chatmind.model.common.ApiResponse;
import org.example.chatmind.model.dto.DocumentDTO;
import org.example.chatmind.model.request.CreateDocumentRequest;
import org.example.chatmind.model.request.UpdateDocumentRequest;
import org.example.chatmind.model.response.CreateDocumentResponse;
import org.example.chatmind.model.response.GetDocumentsResponse;
import org.example.chatmind.model.vo.DocumentVO;
import org.example.chatmind.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ApiResponse<CreateDocumentResponse> create(@RequestBody CreateDocumentRequest request) {
        DocumentDTO dto = convertToDTO(request);
        String documentId = documentService.create(dto);
        return ApiResponse.success(CreateDocumentResponse.builder().documentId(documentId).build());
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody UpdateDocumentRequest request) {
        DocumentDTO dto = convertToDTO(request);
        documentService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        documentService.delete(id);
        return ApiResponse.success();
    }


    @GetMapping("/kb/{kbId}")
    public ApiResponse<GetDocumentsResponse> getByKbId(@PathVariable String kbId) {
        List<DocumentVO> documents = documentService.getByKbId(kbId);
        return ApiResponse.success(GetDocumentsResponse.builder().documents(documents.toArray(new DocumentVO[0])).build());
    }

    @GetMapping
    public ApiResponse<GetDocumentsResponse> getAll() {
        List<DocumentVO> documents = documentService.getAll();
        return ApiResponse.success(GetDocumentsResponse.builder().documents(documents.toArray(new DocumentVO[0])).build());
    }

    @PostMapping("/upload")
    public ApiResponse<CreateDocumentResponse> uploadDocument(
            @RequestParam("kbId") String kbId
            ,@RequestParam("file") MultipartFile file) {
        String documentId = documentService.uploadDocument(kbId, file);
        return ApiResponse.success(CreateDocumentResponse.builder().documentId(documentId).build());
    }
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

    private DocumentDTO convertToDTO(CreateDocumentRequest request) {
        return DocumentDTO.builder()
                .kbId(request.getKbId())
                .filename(request.getFilename())
                .filetype(request.getFiletype())
                .size(request.getSize())
                .build();
    }

    private DocumentDTO convertToDTO(UpdateDocumentRequest request) {
        return DocumentDTO.builder()
                .filename(request.getFilename())
                .filetype(request.getFiletype())
                .size(request.getSize())
                .build();
    }
}
