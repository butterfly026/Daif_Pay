package com.daifubackend.api.service.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Channel;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public interface ChannelService {

    /**分页查询*/
    PageBean page(Integer page, Integer page_size, String name, Short state, boolean isValidAdmin);

    /**批量删除员工*/
    void delete(List<String> ids);

    void deleteById(String id);

    /**新增员工*/
    void save(Channel admin);

    /**根据ID查询员工信息*/
    Channel getById(String id);

    /**更新员工数据*/
    void update(Channel admin);

    List<Channel> getMenus();

    List<Channel> getChannelByCondition(HashMap mapper);

    void updateEncByAdmin(String oldPassword, String newPassword);
}
