package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 部门管理
 */
@Mapper
public interface MemberMapper {
    /**
     * 查询全部部门数据
     * */
    List<Member> list(String username, Short state, String merchant_id);


    /**删除部门*/
    @Delete("delete from wd_members where uid=#{uid}")
    void deleteById(String uid);

    /**新增部门*/
    @Insert("insert into wd_members (uid, username, password, merchant_id, google, created_at, created_by_uid, created_by_name, updated_at, updated_ip, isadmin, state) " +
            "VALUES(#{uid}, #{username}, #{password}, #{merchant_id}, #{google}, #{created_at}, #{created_by_uid}, #{created_by_name}, #{updated_at}, #{updated_ip}, #{isadmin}, #{state})")
    void insert(Member data);

    /**根据id查询*/
    @Select("select * from wd_members where uid=#{uid}")
    Member select(String uid);

    void update(Member data);

    @Select("select * from wd_members where username=#{username}")
    Member getMemberByUsername(String username);
}
