package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Agent;
import com.daifubackend.api.pojo.PageBean;

import java.util.List;

/**
 * 部门管理
 */
public interface AgentService {

    PageBean page(Integer page, Integer page_size, String username, Short state, String merchant_id);

    void delete(String uid);

    void add(Agent data);

    Agent getAgentByUid(String uid);

    void update(Agent data);

    Agent getAgentByUsername(String username);

    List<Agent> getMenus();
}
