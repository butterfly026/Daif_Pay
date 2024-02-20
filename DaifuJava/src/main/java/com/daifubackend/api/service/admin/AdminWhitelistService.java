package com.daifubackend.api.service.admin;

import com.daifubackend.api.pojo.admin.AdminWhitelist;
import com.daifubackend.api.pojo.PageBean;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
public interface AdminWhitelistService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    void delete(String uid);

    void add(AdminWhitelist data);

    AdminWhitelist getDataByUid(String uid);

    void update(AdminWhitelist data);

    int countByAdminId(String ip);

    AdminWhitelist getByIpAdmin(String admin_id, String ip);

    List<AdminWhitelist> getByAdminId(String admin_id);
}
