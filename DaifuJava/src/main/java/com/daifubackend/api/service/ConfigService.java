package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Config;
import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.pojo.PageBean;

import java.util.HashMap;
import java.util.List;

/**
 * 部门管理
 */
public interface ConfigService {

    List<Config> list(HashMap parameters);

    void delete(String key);

    void add(Config data);

    Config getDataByKey(String key);

    void update(Config data);
}
