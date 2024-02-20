package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantWhitelist {
    private String id;
    private String ip;
    private String merchant_id;
    private Integer created_at;
    private String created_by_uid;
    private String created_by_name;
    private Long idx;

}
