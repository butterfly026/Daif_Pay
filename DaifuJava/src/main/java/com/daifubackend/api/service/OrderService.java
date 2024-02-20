package com.daifubackend.api.service;

import com.daifubackend.api.pojo.CashType;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理
 */
public interface OrderService {

    PageBean page(Integer page, Integer page_size, HashMap parameters);

    List<Order> list(HashMap parameters);

    void delete(String uid);

    void add(Order data);

    Order getDataById(String uid);

    void update(Order data);

    void orderChange(String channel_id, String msg, int orderState, String orderId);

    List<Map> getMenu();

    CashType getCashTypeById(String id);
}
