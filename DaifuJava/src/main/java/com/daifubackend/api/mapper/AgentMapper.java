package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Agent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门管理
 */
@Mapper
public interface AgentMapper {
    /**
     * 查询全部部门数据
     * */
    List<Agent> list(String username, Short state, String merchant_id);


    /**删除部门*/
    @Delete("delete from wd_agents where uid=#{uid}")
    void deleteById(String uid);

    /**新增部门*/
    @Insert("insert into wd_agents (uid, username, password, google, created_at, created_by_uid, created_by_name, updated_at, updated_ip, isadmin, state) " +
            "VALUES(#{uid}, #{username}, #{password}, #{google}, #{created_at}, #{created_by_uid}, #{created_by_name}, #{updated_at}, #{updated_ip}, #{isadmin}, #{state})")
    void insert(Agent data);

    /**根据id查询*/
    @Select("select * from wd_agents where uid=#{uid}")
    Agent select(String uid);

    void update(Agent data);

    @Select("select * from wd_agents where username=#{username}")
    Agent getAgentByUsername(String username);

    @Select("select * from wd_agents")
    List<Agent> getMenus();
}
