package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.ChunkBgeM3Mapper;
import org.example.chatmind.mapper.DocumentMapper;
import org.example.chatmind.model.dto.DocumentDTO;
import org.example.chatmind.model.entity.ChunkBgeM3;
import org.example.chatmind.model.entity.Document;
import org.example.chatmind.model.vo.DocumentVO;
import org.example.chatmind.service.DocumentService;
import org.example.chatmind.service.DocumentStorageService;
import org.example.chatmind.service.MarkdownParserService;
import org.example.chatmind.service.RagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final MarkdownParserService markdownParserService;
    private final RagService ragService;
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;

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
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new RuntimeException("Document not found with id: " + id);
        }
        try {
            documentStorageService.deleteFile(document.getMetadata().substring(1, document.getMetadata().length()-1));
        } catch (IOException e) {
            log.warn("删除文件失败，继续删除文档记录: documentId={}, error={}", id, e.getMessage());
        }
        int result = documentMapper.deleteById(id);
        if (result <= 0) {
            throw new BizException("删除document失败");
        }
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

    @Transactional
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
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            int result = documentMapper.insert(document);
            if(result <= 0){
                throw new BizException("创建document失败");
            }

            String documentId = document.getId();

            String documentPath = documentStorageService.saveFile(kbId, documentId, file);

            document.setMetadata("\"" + documentPath + "\"");
            document.setUpdatedAt(LocalDateTime.now());
            document.setCreatedAt(LocalDateTime.now());

            documentMapper.updateById(document);

            log.info("文件上传成功: kbId={}, documentId={}, filename={}, path={}", kbId, documentId, file.getOriginalFilename(), documentPath);


            if("md".equalsIgnoreCase(file.getContentType()) || "markdown".equalsIgnoreCase(file.getContentType())){
                processMarkdownDocument(kbId, documentId, documentPath);
            }else {
                // TODO: 未来可以增加其他文件类型的处理逻辑
                log.warn("待新增处理的文件类型: {}", file.getContentType());
            }

            return documentId;
        } catch (IOException e) {
            log.error("文件上传失败: kbId={}, filename={}", kbId, file.getOriginalFilename(), e);
            throw new BizException("文件上传失败" + e.getMessage());
        }
    }

    private void processMarkdownDocument(String kbId, String documentId, String documentPath) {
        log.info("开始处理markdown文件: kbId={}, documentId={}, path={}", kbId, documentId, documentPath);

        Path filePath = documentStorageService.getFilePath(documentPath);
        try {
            InputStream inputStream = Files.newInputStream(filePath);
            List<MarkdownParserService.MarkdownSection> sections = markdownParserService.parseMarkdown(inputStream);
            System.out.println("sections: " + sections);
            if(sections == null || sections.isEmpty()){
                log.warn("markdown文档没有内容: kbId={}, documentId={}, path={}", kbId, documentId, documentPath);
                return;
            }

            int count = 0;
            for(MarkdownParserService.MarkdownSection section : sections){
                String content = section.getContent();
                String title = section.getTitle();
                if(title == null || title.trim().isEmpty()) continue;
                float[] embed = ragService.embed(title);
                ChunkBgeM3 chunk = ChunkBgeM3.builder()
                        .kbId(kbId)
                        .docId(documentId)
                        .content(content)
                        .metadata(title)
                        .embedding(embed)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                int result = chunkBgeM3Mapper.insert(chunk);
                if (result > 0){
                    count++;
                    log.debug("创建 chunk 成功: title={}, chunkId={}", title, chunk.getId());
                }else {
                    log.warn("创建 chunk 失败: title={}", title);
                }
            }
            log.info("处理 markdown 文档完成: kbId={}, documentId={}, path={}, 创建 chunk 数量={}", kbId, documentId, documentPath, count);
        } catch (IOException e) {
            log.error("处理 Markdown 文档失败: documentId={}", documentId, e);
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
