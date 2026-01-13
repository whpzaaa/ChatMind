package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.*;
import org.example.chatmind.model.entity.Document;

import java.util.List;

@Mapper
public interface DocumentMapper {


    int insert(Document document);


    int updateById(Document document);


    int deleteById(String id);


    Document selectById(String id);


    List<Document> selectByKbId(String kbId);


    List<Document> selectAll();


}
