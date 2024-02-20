package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.ApiWhitelistMapper;
import com.daifubackend.api.pojo.WhiteApi;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.ApiWhitelistService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class ApiWhitelistServiceImpl implements ApiWhitelistService {
    @Autowired
    private ApiWhitelistMapper apiWhitelistMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<WhiteApi> list = apiWhitelistMapper.list(parameters);
        Page<WhiteApi> p = (Page<WhiteApi>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        apiWhitelistMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public WhiteApi getDataByUid(String uid) {
        return apiWhitelistMapper.getById(uid);
    }

    @Override
    public void update(WhiteApi data) {
        apiWhitelistMapper.update(data);
    }

    @Override
    public int countByMerchantId(String id) {
        return apiWhitelistMapper.countByMerchantId(id);
    }

    @Override
    public List<WhiteApi> getByMerchantIp(String merchant_id, String ip) {
        return apiWhitelistMapper.getByIpMerchant(merchant_id, ip);
    }

    @Override
    public void add(WhiteApi data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        apiWhitelistMapper.insert(data);
    }



}
