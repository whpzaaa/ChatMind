package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.chatmind.model.entity.ChunkBgeM3;

import java.util.List;

@Mapper
public interface ChunkBgeM3Mapper {
    int insert(ChunkBgeM3 chunkBgeM3);

    ChunkBgeM3 selectById(String id);

    int updateById(ChunkBgeM3 chunkBgeM3);

    int deleteById(String id);

    List<ChunkBgeM3> similaritySearch(String kbId, String vectorLiteral, int limit);
}
