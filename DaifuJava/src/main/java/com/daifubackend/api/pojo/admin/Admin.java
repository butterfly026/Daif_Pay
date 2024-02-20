package com.daifubackend.api.pojo.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工 实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Admin {
    private String uid; //ID
    private String username; //用户名
    private String password; //密码
    private String group_id;
    private String google;
    private Short state;
    private String created_ip;
    private String created_by_uid;
    private String created_by_name;
    private Integer created_at; //创建时间
    private Integer updated_at; //修改时间
    private Long idx;
}
