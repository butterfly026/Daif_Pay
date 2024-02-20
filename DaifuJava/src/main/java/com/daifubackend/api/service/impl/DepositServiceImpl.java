package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.DepositMapper;
import com.daifubackend.api.mapper.MemberMapper;
import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.DepositService;
import com.daifubackend.api.service.MemberService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class DepositServiceImpl implements DepositService {
    @Autowired
    private DepositMapper depositMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        if(parameters.containsKey("agent_id") && !parameters.get("agent_id").toString().isEmpty()) {
            List<Deposit> list = depositMapper.listByAgent(parameters);
            Page<Deposit> p = (Page<Deposit>) list;
            // 3、封装pageBean对象
            return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
        } else {
            List<Deposit> list = depositMapper.list(parameters);
            Page<Deposit> p = (Page<Deposit>) list;
            // 3、封装pageBean对象
            return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
        }
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        depositMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public Deposit getDataByUid(String uid) {
        return depositMapper.getById(uid);
    }

    @Override
    public Deposit getDataByCondition(HashMap parameters) {
        return depositMapper.getByCondition(parameters);
    }

    @Override
    public void update(Deposit data) {
        depositMapper.update(data);
    }

    @Override
    public void add(Deposit data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        depositMapper.insert(data);
    }



}
