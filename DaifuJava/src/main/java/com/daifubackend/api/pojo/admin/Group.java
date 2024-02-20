package com.daifubackend.api.pojo.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    private String gid;
    private String gname;
    private String permission;
    private String noted;
    private Integer create_at;
    private String pid;
    private Short state;
}
