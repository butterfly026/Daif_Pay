package com.daifubackend.api.service.admin.impl;

import com.daifubackend.api.mapper.admin.AdminMapper;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.admin.AdminService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, String username, String merchant_id, Short state) {
        // 1、获取总记录数
        // 2、获取分页查询结果列表
        // 3、封装在pageBean对象
        // return new PageBean(empMapper.count(), empMapper.page((page-1)*page_size,page_size));

        // 1、设置分页参数
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Admin> list = adminMapper.list(username, merchant_id, state);
        Page<Admin> p = (Page<Admin>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    /**批量删除员工*/
    @Override
    public void delete(List<Integer> ids) {
        adminMapper.delete(ids);
    }

    @Override
    public void deleteByUid(String uid) {
        adminMapper.deleteByUid(uid);
    }

    /**新增员工*/
    @Override
    public void save(Admin admin) {
        admin.setCreated_at((int)(System.currentTimeMillis() / 1000));
        admin.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        adminMapper.insert(admin);
    }

    /**根据ID查询员工信息*/
    @Override
    public Admin getById(String uid) {
        return adminMapper.getById(uid);
    }

    @Override
    public void update(Admin admin) {
        admin.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        adminMapper.update(admin);
    }

    @Override
    public Admin login(Admin admin) {
        return adminMapper.getByUsernameAndPassword(admin);
    }

    @Override
    public Admin getByUsername(String adminName) {
        return adminMapper.getByUsername(adminName);
    }
}
