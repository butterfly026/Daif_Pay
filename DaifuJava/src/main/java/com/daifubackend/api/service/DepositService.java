package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;

import java.util.HashMap;

/**
 * 部门管理
 */
public interface DepositService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    void delete(String uid);

    void add(Deposit data);

    Deposit getDataByUid(String uid);

    Deposit getDataByCondition(HashMap parameters);

    void update(Deposit data);
}
