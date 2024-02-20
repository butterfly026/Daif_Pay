package com.daifubackend.api.service.admin.impl;

import com.daifubackend.api.mapper.admin.GroupMapper;
import com.daifubackend.api.mapper.admin.GroupMapper;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Group;
import com.daifubackend.api.pojo.admin.Group;
import com.daifubackend.api.service.admin.GroupService;
import com.daifubackend.api.service.admin.GroupService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    private GroupMapper mapper;

    @Override
    public List<Group> list() {
        return mapper.list();
    }

    @Override
    public void deleteById(String gid) {
        mapper.delete(gid);
    }


    @Override
    public void save(Group data) {
        data.setCreate_at((int)(System.currentTimeMillis() / 1000));
        mapper.insert(data);

    }

    /**根据ID查询员工信息*/
    @Override
    public Group getById(String gid) {
        return mapper.getById(gid);
    }


    @Override
    public void update(Group data) {
        mapper.update(data);
    }

    @Override
    public List<Map> getPriv() {
        return mapper.getPriv();
    }


}
