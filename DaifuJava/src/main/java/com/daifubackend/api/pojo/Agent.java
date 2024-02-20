package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent {
    private String uid;
    private String username;
    private String password;
    private String google;
    private Integer created_at;
    private String created_by_uid;
    private String created_by_name;
    private Integer updated_at;
    private String updated_ip;
    private Short isadmin;
    private Short state;
    private Long idx;

}
