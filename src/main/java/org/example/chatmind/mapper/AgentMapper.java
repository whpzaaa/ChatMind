package org.example.chatmind.mapper;

import org.apache.ibatis.annotations.*;
import org.example.chatmind.model.entity.Agent;

import java.util.List;

@Mapper
public interface AgentMapper {


    int insert(Agent agent);


    int updateById(Agent agent);


    int deleteById(String id);


    Agent selectById(String id);


    List<Agent> selectAll();

}
