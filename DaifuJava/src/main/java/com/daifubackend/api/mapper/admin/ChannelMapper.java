package com.daifubackend.api.mapper.admin;

import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Channel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * 员工管理
 */
@Mapper
public interface ChannelMapper {

    /**员工信息查询 （统计和分页靠插件）*/
    public List<Channel> list(String name, Short state, boolean isValidAdmin, String password);

    /**批量删除员工*/
    void delete(List<String> ids);

    @Delete("delete from wd_channel where id=#{id}")
    void deleteById(String id);

    /**新增员工*/
    void insert(Channel data);

    /**查询员工*/
    Channel getById(String id, String password);

    void update(Channel data);

    void updateEncByAdmin(String oldPassword, String newPassword);

    List<Channel> getMenus(String password);

    List<Channel> getChannelByCondition(HashMap parameters);


}
