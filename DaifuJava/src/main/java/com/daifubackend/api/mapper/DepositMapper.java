package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Deposit;
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
public interface DepositMapper {
    /**
     * 查询全部部门数据
     * */
    List<Deposit> list(HashMap parameters);

    List<Deposit> listByAgent(HashMap parameters);


    /**删除部门*/
    @Delete("delete from wd_deposit where id=#{id}")
    void deleteById(String id);

    /**新增部门*/
    @Insert("insert into wd_deposit (id, amount, flags, merchant_id, created_at, created_ip, review_at, review_by_uid, review_by_name, created_by_uid, created_by_name, merchant_remark, review_remark, state, order_no, channel_id, api_request_json, channel_request_json, channel_response_json, channel_notify_resp_json, sent_notify_cnt) " +
            "VALUES (#{id}, #{amount}, #{flags}, #{merchant_id}, #{created_at}, #{created_ip}, #{review_at}, #{review_by_uid}, #{review_by_name}, #{created_by_uid}, #{created_by_name}, #{merchant_remark}, #{review_remark}, #{state}, #{order_no}, #{channel_id}, #{api_request_json}, #{channel_request_json}, #{channel_response_json}, #{channel_notify_resp_json}, #{sent_notify_cnt})")
    void insert(Deposit data);

    /**根据id查询*/
    @Select("select * from wd_deposit where id=#{id}")
    Deposit getById(String id);

    void update(Deposit data);

    Deposit getByCondition(HashMap parameters);

}
