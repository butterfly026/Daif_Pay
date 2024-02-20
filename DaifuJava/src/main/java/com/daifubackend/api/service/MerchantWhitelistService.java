package com.daifubackend.api.service;

import com.daifubackend.api.pojo.MerchantWhitelist;
import com.daifubackend.api.pojo.PageBean;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
public interface MerchantWhitelistService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    void delete(String uid);

    void add(MerchantWhitelist data);

    MerchantWhitelist getDataByUid(String uid);

    void update(MerchantWhitelist data);

    int countByMerchantId(String ip);

    MerchantWhitelist getByMerchantIp(String merchant_id, String ip);

    List<MerchantWhitelist> getByMerchantId(String merchant_id);
}
