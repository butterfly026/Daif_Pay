package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.MerchantWhitelist;
import com.daifubackend.api.pojo.Member;
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
public interface MerchantWhitelistMapper {
    /**
     * 查询全部部门数据
     * */
    List<MerchantWhitelist> list(HashMap parameters);


    /**删除部门*/
    @Delete("delete from wd_merchant_whitelist where id=#{id}")
    void deleteById(String id);

    /**新增部门*/
    @Insert("insert into wd_merchant_whitelist (id, ip, merchant_id, created_at, created_by_uid, created_by_name) " +
            "VALUES (#{id}, #{ip}, #{merchant_id}, #{created_at}, #{created_by_uid}, #{created_by_name})")
    void insert(MerchantWhitelist data);

    /**根据id查询*/
    @Select("select * from wd_merchant_whitelist where id=#{id}")
    MerchantWhitelist getById(String id);

    void update(MerchantWhitelist data);

    @Select("select count(*) from wd_merchant_whitelist where merchant_id=#{id}")
    int countByMerchantId(String id);

    @Select("select * from wd_merchant_whitelist where merchant_id=#{merchant_id} and ip like concat('%', #{ip}, '%')")
    MerchantWhitelist getByIpMerchant(String merchant_id, String ip);
}
