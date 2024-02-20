package com.daifubackend.api.service.admin.impl;

import com.daifubackend.api.mapper.admin.MerchantMapper;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.admin.MerchantService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Autowired
    private MerchantMapper mapper;

    @Override
    public PageBean page(Integer page, Integer page_size, String name, Short state, String channel_id) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Merchant> list = mapper.list(name,state, channel_id);
        Page<Merchant> p = (Page<Merchant>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Override
    public PageBean page(Integer page, Integer page_size, String name, Short state, String channel_id, String agent_id) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Merchant> list = mapper.listForAgent(name,state, channel_id, agent_id);
        Page<Merchant> p = (Page<Merchant>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    /**批量删除员工*/
    @Override
    public void delete(List<String> ids) {
        mapper.delete(ids);
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void save(Merchant data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        mapper.insert(data);

    }

    /**根据ID查询员工信息*/
    @Override
    public Merchant getById(String id) {
        return mapper.getById(id);
    }


    @Override
    public void update(Merchant data) {
        mapper.update(data);
    }

    @Override
    public List<Merchant> getMenus() {
        return mapper.getMenus();
    }

    @Override
    public List<Merchant> getByAgentId(String uid) {
        return mapper.getByAgentId(uid);
    }


}
