package org.example.chatmind.service;

import org.example.chatmind.model.dto.KnowledgeBaseDTO;
import org.example.chatmind.model.vo.KnowledgeBaseVO;

import java.util.List;

public interface KnowledgeBaseService {

    String create(KnowledgeBaseDTO dto);

    void update(String id, KnowledgeBaseDTO dto);

    void delete(String id);

//    KnowledgeBaseVO getById(String id);

    List<KnowledgeBaseVO> getAll();

//    List<KnowledgeBaseVO> getByPage(int pageNum, int pageSize);
//
//    int count();
}
