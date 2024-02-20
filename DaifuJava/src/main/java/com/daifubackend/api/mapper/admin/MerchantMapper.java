package com.daifubackend.api.mapper.admin;

import com.daifubackend.api.pojo.admin.Merchant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工管理
 */
@Mapper
public interface MerchantMapper {

    /**员工信息查询 （统计和分页靠插件）*/
    //@Select("select * from wd_Merchant")
    public List<Merchant> list(String name, Short state, String channel_id);

    public List<Merchant> listForAgent(String name, Short state, String channel_id, String agent_id);

    /**批量删除员工*/
    void delete(List<String> ids);

    @Delete("delete from wd_merchant where id=#{id}")
    void deleteById(String id);

    /**新增员工*/
    @Insert("insert into wd_merchant (id, channel_id, agent_id, agent_ratio, name, ppk, balance, withdraw_fee, withdraw_scale, deposit_bank_fee, m_single_withdraw, m_batch_withdraw, api_withdraw, remarks, created_at, state, confirm, min_limit, max_limit, need_reverse_check) " +
            " VALUES (#{id}, #{channel_id}, #{agent_id}, #{agent_ratio}, #{name}, #{ppk}, #{balance}, #{withdraw_fee}, #{withdraw_scale}, #{deposit_bank_fee}, #{m_single_withdraw}, #{m_batch_withdraw}, #{api_withdraw}, #{remarks}, #{created_at}, #{state}, #{confirm}, #{min_limit}, #{max_limit}, #{need_reverse_check}) ")
    void insert(Merchant data);

    /**查询员工*/
    @Select("select * from wd_merchant where id = #{id}")
    Merchant getById(String id);

    void update(Merchant data);

    @Select("select * from wd_merchant")
    List<Merchant> getMenus();

    @Select("select * from wd_merchant where agent_id=#{uid}")
    List<Merchant> getByAgentId(String uid);
}
