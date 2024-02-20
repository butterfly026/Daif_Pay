package com.daifubackend.api.mapper;

import com.daifubackend.api.pojo.CashType;
import com.daifubackend.api.pojo.Order;
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
public interface OrderMapper {
    /**
     * 查询全部部门数据
     * */
    List<Order> list(HashMap parameters);

    List<Order> listByAgent(HashMap parameters);
    /**删除部门*/
    @Delete("delete from wd_orders where id=#{id}")
    void deleteById(String id);

    /**新增部门*/
    @Insert("insert into wd_orders (id, flags, channel_id, merchant_serial, merchant_id, merchant_name, apply_amount, actually_amount, fee, created_at, created_ip, bank_type_name, bank_name, bank_card, bank_opening, callback_url, state, review_by_uid, review_at, review_by_name, notify_state, err) " +
            "VALUES (#{id}, #{flags}, #{channel_id}, #{merchant_serial}, #{merchant_id}, #{merchant_name}, #{apply_amount}, #{actually_amount}, #{fee}, #{created_at}, #{created_ip}, #{bank_type_name}, #{bank_name}, #{bank_card}, #{bank_opening}, #{callback_url}, #{state}, #{review_by_uid}, #{review_at}, #{review_by_name}, #{notify_state}, #{err})")
    void insert(Order data);

    /**根据id查询*/
    @Select("select * from wd_orders where id=#{id}")
    Order getById(String id);

    void update(Order data);

    @Select("select id,name from wd_orders where state=1 order by created_at desc")
    List<Map> getMenu();

    @Select("select * from wd_cash_type where id=#{id}")
    CashType getCashTypeById(String id);
}
