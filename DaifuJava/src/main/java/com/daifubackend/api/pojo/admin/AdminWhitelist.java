package com.daifubackend.api.pojo.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminWhitelist {
    private String id;
    private String ip;
    private String admin_id;
    private Integer created_at;
    private String created_by_uid;
    private String created_by_name;
    private Long idx;

}
