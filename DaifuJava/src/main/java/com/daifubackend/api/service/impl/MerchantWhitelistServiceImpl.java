package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.MerchantWhitelistMapper;
import com.daifubackend.api.pojo.MerchantWhitelist;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.MerchantWhitelistService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class MerchantWhitelistServiceImpl implements MerchantWhitelistService {
    @Autowired
    private MerchantWhitelistMapper merchantWhitelistMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<MerchantWhitelist> list = merchantWhitelistMapper.list(parameters);
        Page<MerchantWhitelist> p = (Page<MerchantWhitelist>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        merchantWhitelistMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public MerchantWhitelist getDataByUid(String uid) {
        return merchantWhitelistMapper.getById(uid);
    }

    @Override
    public void update(MerchantWhitelist data) {
        merchantWhitelistMapper.update(data);
    }

    @Override
    public int countByMerchantId(String id) {
        return merchantWhitelistMapper.countByMerchantId(id);
    }

    @Override
    public MerchantWhitelist getByMerchantIp(String merchant_id, String ip) {
        return merchantWhitelistMapper.getByIpMerchant(merchant_id, ip);
    }

    @Override
    public List<MerchantWhitelist> getByMerchantId(String merchant_id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("merchant_id", merchant_id);
        return merchantWhitelistMapper.list(params);
    }

    @Override
    public void add(MerchantWhitelist data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        merchantWhitelistMapper.insert(data);
    }



}
