package org.example.chatmind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.chatmind.exception.BizException;
import org.example.chatmind.mapper.KnowledgeBaseMapper;
import org.example.chatmind.model.dto.KnowledgeBaseDTO;
import org.example.chatmind.model.entity.KnowledgeBase;
import org.example.chatmind.model.vo.KnowledgeBaseVO;
import org.example.chatmind.service.KnowledgeBaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String create(KnowledgeBaseDTO dto) {
        KnowledgeBase knowledgeBase = convertToEntity(dto);
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        int result = knowledgeBaseMapper.insert(knowledgeBase);
        if(result <= 0){
            throw new BizException("创建知识库失败");
        }
        return knowledgeBase.getId();
    }

    @Override
    public void update(String id, KnowledgeBaseDTO dto) {
        KnowledgeBase existingKnowledgeBase = knowledgeBaseMapper.selectById(id);
        if (existingKnowledgeBase == null) {
            throw new RuntimeException("KnowledgeBase not found with id: " + id);
        }

        KnowledgeBase knowledgeBase = convertToEntity(dto);
        knowledgeBase.setId(id);
        knowledgeBase.setCreatedAt(existingKnowledgeBase.getCreatedAt());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    @Override
    public void delete(String id) {
        knowledgeBaseMapper.deleteById(id);
    }

//    @Override
//    public KnowledgeBaseVO getById(String id) {
//        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(id);
//        if (knowledgeBase == null) {
//            throw new RuntimeException("KnowledgeBase not found with id: " + id);
//        }
//        return convertToVO(knowledgeBase);
//    }

    @Override
    public List<KnowledgeBaseVO> getAll() {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectAll();
        return knowledgeBases.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<KnowledgeBaseVO> getByPage(int pageNum, int pageSize) {
//        int offset = (pageNum - 1) * pageSize;
//        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectByPage(offset, pageSize);
//        return knowledgeBases.stream()
//                .map(this::convertToVO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public int count() {
//        return knowledgeBaseMapper.count();
//    }
//
    private KnowledgeBase convertToEntity(KnowledgeBaseDTO dto) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        BeanUtils.copyProperties(dto, knowledgeBase, "metadata");

        try {
            if (dto.getMetadata() != null) {
                knowledgeBase.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        return knowledgeBase;
    }

    private KnowledgeBaseVO convertToVO(KnowledgeBase knowledgeBase) {
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, vo);
        return vo;
    }
}
