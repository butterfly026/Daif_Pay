package com.daifubackend.api.mapper.admin;

import com.daifubackend.api.pojo.admin.Admin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工管理
 */
@Mapper
public interface AdminMapper {
//    /**
//     * 查询总记录数
//     */
//    @Select("select count(*) from emp")
//    public Long count();
//
//    /**
//     * 分页查询，获取列表数据
//     */
//    @Select("select * from emp limit #{start},#{page_size}")
//    public List<Emp> page(Integer start, Integer page_size);

    /**员工信息查询 （统计和分页靠插件）*/
    //@Select("select * from wd_admin")
    public List<Admin> list(String username, String group_id, Short state);

    /**批量删除员工*/
    void delete(List<Integer> ids);
    @Delete("delete from wd_admin where uid=#{id}")
    void deleteByUid(String id);

    /**新增员工*/
    @Insert("insert into wd_admin(uid, username, password, group_id, google, state, created_ip, created_by_uid, created_by_name, created_at, updated_at) " +
            "VALUES (#{uid},#{username},#{password},#{group_id},#{google},#{state},#{created_ip},#{created_by_uid},#{created_by_name}, #{created_at}, #{updated_at})")
    void insert(Admin admin);

    /**查询员工*/
    @Select("select * from wd_admin where uid = #{uid}")
    Admin getById(String id);

    void update(Admin admin);

    @Select("select * from wd_admin where username = #{username} and password = #{password}")
    Admin getByUsernameAndPassword(Admin admin);


    /**根据部门ID删除该部门下的员工数据*/
    @Delete("delete from wd_admin where dept_id = #{deptId}")
    public void deleteByDeptId(Integer deptId);

    @Select("select * from wd_admin where username = #{username}")
    Admin getByUsername(String adminName);
}
