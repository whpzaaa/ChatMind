package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.*;
import org.example.chatmind.model.entity.KnowledgeBase;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {


    int insert(KnowledgeBase knowledgeBase);


    int updateById(KnowledgeBase knowledgeBase);


    int deleteById(String id);


    KnowledgeBase selectById(String id);


    List<KnowledgeBase> selectAll();

    List<KnowledgeBase> selectByIdBatch(List<String> ids);

}
