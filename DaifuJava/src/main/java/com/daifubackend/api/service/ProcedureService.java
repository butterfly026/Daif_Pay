package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;

import java.util.List;
import java.util.Map;

/**
 * 部门管理
 */
public interface ProcedureService {

    Map createOrder(Map parameters);
    Map creditProcedure(Map parameters);

    List<Map> selectByQuery(String query);
}
