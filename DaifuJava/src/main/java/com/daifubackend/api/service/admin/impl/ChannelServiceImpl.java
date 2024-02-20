package com.daifubackend.api.service.admin.impl;

import com.daifubackend.api.mapper.admin.ChannelMapper;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.service.admin.AdminService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ChannelServiceImpl implements ChannelService {
    @Autowired
    private ChannelMapper mapper;

    @Autowired
    private AdminService adminService;

    @Override
    public PageBean page(Integer page, Integer page_size, String name, Short state, boolean isValidAdmin) {
        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return new PageBean(0L, 1, new ArrayList<>());
        }
        String password = admin.getPassword();
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Channel> list = mapper.list(name,state, isValidAdmin, password);
        Page<Channel> p = (Page<Channel>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(),  p.getResult());
    }

    /**批量删除员工*/
    @Override
    public void delete(List<String> ids) {
        mapper.delete(ids);
    }

    @Override
    public void deleteById(String id) { mapper.deleteById(id); }

    @Override
    public void save(Channel data) {
        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return;
        }
        String password = admin.getPassword();
        data.setPassword(password);
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        mapper.insert(data);

    }

    /**根据ID查询员工信息*/
    @Override
    public Channel getById(String id) {
        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return null;
        }
        String password = admin.getPassword();
        return mapper.getById(id, password);
    }


    @Override
    public void update(Channel data) {
        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return;
        }
        String password = admin.getPassword();
        data.setPassword(password);
        mapper.update(data);
    }

    @Override
    public List<Channel> getMenus() {

        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return null;
        }
        String password = admin.getPassword();
        return mapper.getMenus(password);
    }

    @Override
    public List<Channel> getChannelByCondition(HashMap parameters) {
        Admin admin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        if(admin == null) {
            log.error("Channel Admin(" + GlobalConsts.ChannelAdminName + ") exist!");
            return null;
        }
        String password = admin.getPassword();
        parameters.put("password", password);
        return mapper.getChannelByCondition(parameters);
    }

    @Override
    public void updateEncByAdmin(String oldPassword, String newPassword){
        mapper.updateEncByAdmin(oldPassword, newPassword);
    }


}
