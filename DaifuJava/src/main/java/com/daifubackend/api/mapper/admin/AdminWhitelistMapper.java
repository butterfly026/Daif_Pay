package com.daifubackend.api.mapper.admin;

import com.daifubackend.api.pojo.admin.AdminWhitelist;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
@Mapper
public interface AdminWhitelistMapper {
    /**
     * 查询全部部门数据
     * */
    List<AdminWhitelist> list(HashMap parameters);


    /**删除部门*/
    @Delete("delete from wd_admin_whitelist where id=#{id}")
    void deleteById(String id);

    /**新增部门*/
    @Insert("insert into wd_admin_whitelist (id, ip, admin_id, created_at, created_by_uid, created_by_name) " +
            "VALUES (#{id}, #{ip}, #{admin_id}, #{created_at}, #{created_by_uid}, #{created_by_name})")
    void insert(AdminWhitelist data);

    /**根据id查询*/
    @Select("select * from wd_admin_whitelist where id=#{id}")
    AdminWhitelist getById(String id);

    void update(AdminWhitelist data);

    @Select("select count(*) from wd_admin_whitelist where admin_id=#{id}")
    int countByAdminId(String id);

    @Select("select * from wd_admin_whitelist where admin_id=#{admin_id} and ip like concat('%', #{ip}, '%')")
    AdminWhitelist getByIpAdmin(String admin_id, String ip);
}
