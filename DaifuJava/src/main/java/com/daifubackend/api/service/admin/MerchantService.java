package com.daifubackend.api.service.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;

import java.util.List;

public interface MerchantService {

    /**分页查询*/
    PageBean page(Integer page, Integer page_size, String name, Short state, String channel_id);

    PageBean page(Integer page, Integer page_size, String name, Short state, String channel_id, String agent_id);

    /**批量删除员工*/
    void delete(List<String> ids);

    void deleteById(String id);

    /**新增员工*/
    void save(Merchant admin);

    /**根据ID查询员工信息*/
    Merchant getById(String id);

    /**更新员工数据*/
    void update(Merchant admin);

    List<Merchant> getMenus();

    List<Merchant> getByAgentId(String uid);
}
