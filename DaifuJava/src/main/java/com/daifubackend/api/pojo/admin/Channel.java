package com.daifubackend.api.pojo.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel {
    private String id;
    private String name;
    private String account;
    private String ppk;
    private String gateway;
    private Float rate;
    private Integer created_at;
    private Short state;
    private String notify_url;
    private String proxy;
    private String shortname;
    private Long idx;
    private String password;
    private String other_info;
}
