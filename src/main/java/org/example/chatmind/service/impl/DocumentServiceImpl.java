package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.DocumentMapper;
import org.example.chatmind.model.dto.DocumentDTO;
import org.example.chatmind.model.entity.Document;
import org.example.chatmind.model.vo.DocumentVO;
import org.example.chatmind.service.DocumentService;
import org.example.chatmind.service.DocumentStorageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper;
    private final DocumentStorageService documentStorageService;

    @Override
    public String create(DocumentDTO dto) {
        Document document = convertToEntity(dto);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        int result = documentMapper.insert(document);
        if (result <= 0) {
            throw new BizException("创建document失败");
        }
        return document.getId();
    }

    @Override
    public void update(String id, DocumentDTO dto) {
        Document existingDocument = documentMapper.selectById(id);
        if (existingDocument == null) {
            throw new RuntimeException("Document not found with id: " + id);
        }

        Document document = convertToEntity(dto);
        document.setId(id);
        document.setCreatedAt(existingDocument.getCreatedAt());
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.updateById(document);
    }

    @Override
    public void delete(String id) {
        documentMapper.deleteById(id);
    }

    @Override
    public DocumentVO getById(String id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found with id: " + id);
        }
        return convertToVO(document);
    }

    @Override
    public List<DocumentVO> getByKbId(String kbId) {
        List<Document> documents = documentMapper.selectByKbId(kbId);
        return documents.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentVO> getAll() {
        List<Document> documents = documentMapper.selectAll();
        return documents.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public String uploadDocument(String kbId, MultipartFile file) {
        try {
            if(file.isEmpty()){
                throw new BizException("上传的文件为空");
            }
            Document document = Document.builder()
                    .kbId(kbId)
                    .filename(file.getOriginalFilename())
                    .filetype(getFileType(file))
                    .size(file.getSize())
                    .metadata("")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            int result = documentMapper.insert(document);
            if(result <= 0){
                throw new BizException("创建document失败");
            }

            String documentId = document.getId();

            String documentPath = documentStorageService.saveFile(kbId, documentId, file);

            document.setMetadata(documentPath);
            document.setUpdatedAt(LocalDateTime.now());
            document.setCreatedAt(LocalDateTime.now());

            documentMapper.updateById(document);

            log.info("文件上传成功: kbId={}, documentId={}, filename={}, path={}", kbId, documentId, file.getOriginalFilename(), documentPath);

            //todo:解析md文件 生成chunks

            return documentId;
        } catch (IOException e) {
            log.error("文件上传失败: kbId={}, filename={}", kbId, file.getOriginalFilename(), e);
            throw new BizException("文件上传失败" + e.getMessage());
        }
    }

    private String getFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null || !originalFilename.contains(".")){
            return "unknown";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }

    //    @Override
//    public List<DocumentVO> getByPage(int pageNum, int pageSize) {
//        int offset = (pageNum - 1) * pageSize;
//        List<Document> documents = documentMapper.selectByPage(offset, pageSize);
//        return documents.stream()
//                .map(this::convertToVO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public int count() {
//        return documentMapper.count();
//    }
//
//    @Override
//    public int countByKbId(String kbId) {
//        return documentMapper.countByKbId(kbId);
//    }
//
    private Document convertToEntity(DocumentDTO dto) {
        Document document = new Document();
        BeanUtils.copyProperties(dto, document, "metadata");

        try {
            if (dto.getMetadata() != null) {
                document.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        return document;
    }

    private DocumentVO convertToVO(Document document) {
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(document, vo);
        return vo;
    }
}
