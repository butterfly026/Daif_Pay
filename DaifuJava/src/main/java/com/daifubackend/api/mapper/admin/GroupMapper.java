package com.daifubackend.api.mapper.admin;

import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Group;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@Mapper
public interface GroupMapper {

    /**员工信息查询 （统计和分页靠插件）*/
    @Select("select * from wd_admin_group where state = 1")
    List<Group> list();

    @Select("select * from wd_admin_priv")
    List<Map> getPriv();

    /**批量删除员工*/
    @Delete("delete from wd_admin_group where gid=#{gid}")
    void delete(String gid);

    /**新增员工*/
    @Insert("insert into wd_admin_group (gid, gname, permission, noted, create_at, pid, state) " +
            " VALUES (#{gid}, #{gname}, #{permission}, #{noted}, #{create_at}, #{pid}, #{state}) ")
    void insert(Group data);

    /**查询员工*/
    @Select("select * from wd_admin_group where gid = #{gid}")
    Group getById(String gid);

    void update(Group data);

}
