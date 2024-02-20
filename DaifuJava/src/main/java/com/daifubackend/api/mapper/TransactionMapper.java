package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.Transaction;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理
 */
@Mapper
public interface TransactionMapper {
    /**
     * 查询全部部门数据
     * */
    List<Transaction> list(HashMap parameters);


    /**删除部门*/
    @Delete("delete from wd_transaction where id=#{id}")
    void deleteById(String id);

    /**新增部门*/
    @Insert("insert into wd_transaction (id, amount, flags, merchant_id, created_at, created_ip, review_at, review_by_uid, review_by_name, created_by_uid, created_by_name, merchant_remark, review_remark, state) " +
            "VALUES (#{id}, #{amount}, #{flags}, #{merchant_id}, #{created_at}, #{created_ip}, #{review_at}, #{review_by_uid}, #{review_by_name}, #{created_by_uid}, #{created_by_name}, #{merchant_remark}, #{review_remark}, #{state})")
    void insert(Transaction data);

    /**根据id查询*/
    @Select("select * from wd_transaction where id=#{id}")
    Transaction getById(String id);

    void update(Transaction data);

    List<Map> getMenu();

}
