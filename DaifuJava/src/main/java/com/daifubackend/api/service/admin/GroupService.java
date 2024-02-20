package com.daifubackend.api.service.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Group;
import com.daifubackend.api.pojo.admin.Group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface GroupService {

    /**分页查询*/
    List<Group> list();

    /**批量删除员工*/    

    void deleteById(String gid);

    /**新增员工*/
    void save(Group data);

    /**根据ID查询员工信息*/
    Group getById(String id);

    /**更新员工数据*/
    void update(Group admin);

    List<Map> getPriv();

}
