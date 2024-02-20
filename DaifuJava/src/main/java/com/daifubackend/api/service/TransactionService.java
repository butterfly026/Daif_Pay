package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理
 */
public interface TransactionService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    void delete(String uid);

    void add(Transaction data);

    Transaction getDataById(String uid);

    void update(Transaction Member);

    List<Map> getMenu();
}
