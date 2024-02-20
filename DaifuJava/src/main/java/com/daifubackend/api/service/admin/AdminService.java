package com.daifubackend.api.service.admin;

import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.PageBean;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工管理
 */
public interface AdminService {
    /**分页查询*/
    PageBean page(Integer page, Integer page_size, String username, String group_id, Short state);

    /**批量删除员工*/
    void delete(List<Integer> ids);

    void deleteByUid(String uid);

    /**新增员工*/
    void save(Admin admin);

    /**根据ID查询员工信息*/
    Admin getById(String uid);

    /**更新员工数据*/
    void update(Admin admin);

    /**用户登录*/
    Admin login(Admin admin);

    Admin getByUsername(String channelAdminName);

}
