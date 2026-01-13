package org.example.chatmind.service;

import org.example.chatmind.model.dto.DocumentDTO;
import org.example.chatmind.model.vo.DocumentVO;

import java.util.List;

public interface DocumentService {

    String create(DocumentDTO dto);

    void update(String id, DocumentDTO dto);

    void delete(String id);

    DocumentVO getById(String id);

    List<DocumentVO> getByKbId(String kbId);

    List<DocumentVO> getAll();

//    List<DocumentVO> getByPage(int pageNum, int pageSize);
//
//    int count();
//
//    int countByKbId(String kbId);
}
