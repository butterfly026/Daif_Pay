package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.ConfigMapper;
import com.daifubackend.api.mapper.DepositMapper;
import com.daifubackend.api.pojo.Config;
import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.ConfigService;
import com.daifubackend.api.service.DepositService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private ConfigMapper configMapper;


    @Override
    public List<Config> list(HashMap parameters) {
        return configMapper.list(parameters);
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String key){
        configMapper.deleteByKey(key);      //根据ID删除部门
    }

    @Override
    public void add(Config data) {
        configMapper.insert(data);
    }

    @Override
    public Config getDataByKey(String key) {
        return configMapper.getByKey(key);
    }

    @Override
    public void update(Config data) {
        configMapper.update(data);
    }


}
