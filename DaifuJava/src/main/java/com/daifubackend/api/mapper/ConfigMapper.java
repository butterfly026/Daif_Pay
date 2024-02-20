package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Config;
import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Member;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
@Mapper
public interface ConfigMapper {
    /**
     * 查询全部部门数据
     * */
    List<Config> list(HashMap parameters);


    /**删除部门*/
    @Delete("delete from configs where `key`=#{key}")
    void deleteByKey(String id);

    /**新增部门*/
    @Insert("insert into configs (`key`, `value`) " +
            "VALUES (#{key}, #{value})")
    void insert(Config data);

    /**根据id查询*/
    @Select("select * from configs where id=#{id}")
    Config getById(String id);

    @Select("select * from configs where `key`=#{key} limit 1")
    Config getByKey(String key);

    void update(Config data);

}
