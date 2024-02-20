package com.daifubackend.api.service;

import com.daifubackend.api.pojo.WhiteApi;
import com.daifubackend.api.pojo.PageBean;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
public interface ApiWhitelistService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    void delete(String uid);

    void add(WhiteApi data);

    WhiteApi getDataByUid(String uid);

    void update(WhiteApi data);

    int countByMerchantId(String ip);

    List<WhiteApi> getByMerchantIp(String merchant_id, String ip);
}
