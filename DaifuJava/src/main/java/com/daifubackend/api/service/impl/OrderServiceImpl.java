package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.OrderMapper;
import com.daifubackend.api.pojo.CashType;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.service.OrderService;
import com.daifubackend.api.utils.CommonUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        if(parameters.containsKey("agent_id") && !parameters.get("agent_id").toString().isEmpty()) {
            List<Order> list = orderMapper.listByAgent(parameters);
            Page<Order> p = (Page<Order>) list;
            // 3、封装pageBean对象
            return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
        } else {
            List<Order> list = orderMapper.list(parameters);
            Page<Order> p = (Page<Order>) list;
            // 3、封装pageBean对象
            return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
        }

    }

    @Override
    public List<Order> list(HashMap parameters) {
        return orderMapper.list(parameters);
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        orderMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public void add(Order data) {
        orderMapper.insert(data);
    }

    @Override
    public Order getDataById(String uid) {
        return orderMapper.getById(uid);
    }

    @Override
    public void update(Order data) {
        orderMapper.insert(data);

    }

    @Override
    public void orderChange(String channel_id, String msg, int orderState, String orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setErr(msg + " ");
        if(CommonUtils.parseLong(channel_id) != 0) {
            order.setChannel_id(channel_id);
        }
        if(orderState != -1) {
            order.setState(orderState);
        }
        orderMapper.update(order);
    }

    @Override
    public List<Map> getMenu() {
        return orderMapper.getMenu();
    }

    @Override
    public CashType getCashTypeById(String id) {
        return orderMapper.getCashTypeById(id);
    }


}
