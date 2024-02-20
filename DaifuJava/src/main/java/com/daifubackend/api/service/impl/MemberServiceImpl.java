package com.daifubackend.api.service.impl;

import com.daifubackend.api.mapper.MemberMapper;
import com.daifubackend.api.mapper.admin.AdminMapper;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.service.MemberService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    private MemberMapper memberMapper;

    @Override
    public PageBean page(Integer page, Integer page_size, String username, Short state, String merchant_id) {
        PageHelper.startPage(page, page_size);
        // 2、执行查询
        List<Member> list = memberMapper.list(username,state, merchant_id);
        Page<Member> p = (Page<Member>) list;
        // 3、封装pageBean对象
        return new PageBean(p.getTotal(), p.getPageSize(), p.getResult());
    }

    @Transactional(rollbackFor = Exception.class)   //默认情况下只有运行时异常才会被回滚 rollbackFor指定所有异常都回滚
    @Override
    public void delete(String uid){
        memberMapper.deleteById(uid);      //根据ID删除部门
    }

    @Override
    public void add(Member data) {
        data.setCreated_at((int)(System.currentTimeMillis() / 1000));
        data.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        memberMapper.insert(data);
    }

    @Override
    public Member getMemberByUid(String uid) {
        return memberMapper.select(uid);
    }

    @Override
    public void update(Member data) {
        data.setUpdated_at((int)(System.currentTimeMillis() / 1000));
        memberMapper.update(data);
    }

    @Override
    public Member getMemberByUsername(String username) {
        return memberMapper.getMemberByUsername(username);
    }
}
