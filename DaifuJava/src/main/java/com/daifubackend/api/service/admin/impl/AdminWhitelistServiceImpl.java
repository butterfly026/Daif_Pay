package com.daifubackend.api.service.admin.impl;

import com.daifubackend.api.mapper.admin.AdminWhitelistMapper;
import com.daifubackend.api.pojo.admin.AdminWhitelist;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.admin.AdminWhitelistService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class AdminWhitelistServiceImpl implements AdminWhitelistService {
    @Autowired
    private AdminWhitelistMapper AdminWhitelistMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<AdminWhitelist> list = AdminWhitelistMapper.list(parameters);
        Page<AdminWhitelist> p = (Page<AdminWhitelist>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        AdminWhitelistMapper.deleteById(uid);
    }

    @Override
    public AdminWhitelist getDataByUid(String uid) {
        return AdminWhitelistMapper.getById(uid);
    }

    @Override
    public void update(AdminWhitelist data) {
        AdminWhitelistMapper.update(data);
    }

    @Override
    public int countByAdminId(String id) {
        return AdminWhitelistMapper.countByAdminId(id);
    }

    @Override
    public AdminWhitelist getByIpAdmin(String admin_id, String ip) {
        return AdminWhitelistMapper.getByIpAdmin(admin_id, ip);
    }

    @Override
    public List<AdminWhitelist> getByAdminId(String admin_id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("admin_id", admin_id);
        return AdminWhitelistMapper.list(params);
    }

    @Override
    public void add(AdminWhitelist data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        AdminWhitelistMapper.insert(data);
    }

}
