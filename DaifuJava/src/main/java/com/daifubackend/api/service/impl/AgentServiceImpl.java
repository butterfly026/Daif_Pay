package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.AgentMapper;
import com.daifubackend.api.pojo.Agent;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.AgentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {
    @Autowired
    private AgentMapper agentMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, String username, Short state, String merchant_id) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Agent> list = agentMapper.list(username,state, merchant_id);
        Page<Agent> p = (Page<Agent>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        agentMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public void add(Agent data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        data.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        agentMapper.insert(data);
    }

    @Override
    public Agent getAgentByUid(String uid) {
        return agentMapper.select(uid);
    }

    @Override
    public void update(Agent data) {
        data.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        agentMapper.update(data);
    }

    @Override
    public Agent getAgentByUsername(String username) {
        return agentMapper.getAgentByUsername(username);
    }

    @Override
    public List<Agent> getMenus() {
        return agentMapper.getMenus();
    }
}
