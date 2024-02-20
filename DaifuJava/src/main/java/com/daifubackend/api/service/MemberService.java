package com.daifubackend.api.service;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;

/**
 * 部门管理
 */
public interface MemberService {

    PageBean page(Integer page, Integer page_size, String username, Short state, String merchant_id);

    void delete(String uid);

    void add(Member data);

    Member getMemberByUid(String uid);

    void update(Member data);

    Member getMemberByUsername(String username);
}
