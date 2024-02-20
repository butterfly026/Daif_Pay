package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.TransactionMapper;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Transaction;
import com.daifubackend.api.service.TransactionService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, HashMap parameters) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Transaction> list = transactionMapper.list(parameters);
        Page<Transaction> p = (Page<Transaction>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        transactionMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public void add(Transaction data) {
        transactionMapper.insert(data);
    }

    @Override
    public Transaction getDataById(String uid) {
        return transactionMapper.getById(uid);
    }

    @Override
    public void update(Transaction data) {
        transactionMapper.insert(data);

    }

    @Override
    public List<Map> getMenu() {
        return transactionMapper.getMenu();
    }


}
