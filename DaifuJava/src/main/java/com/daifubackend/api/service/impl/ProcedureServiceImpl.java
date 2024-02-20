package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.MemberMapper;
import com.daifubackend.api.mapper.ProcedureMapper;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.ProcedureService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProcedureServiceImpl implements ProcedureService {
    @Autowired
    private ProcedureMapper procedureMapper;


    @Override
    public Map createOrder(Map parameters) {
        return procedureMapper.createOrder(parameters);
    }

    @Override
    public Map creditProcedure(Map parameters) {
        return procedureMapper.creditProcedure(parameters);
    }

    @Override
    public List<Map> selectByQuery(String query){ return procedureMapper.selectByQuery(query);}
}
