package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Member;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理
 */
@Mapper
public interface ProcedureMapper {

    Map createOrder(Map parameters);

    Map creditProcedure(Map parameters);

    @Select("select ${query}")
    List<Map> selectByQuery(String query);
}
