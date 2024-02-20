package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String id;
    private String order_id;
    private Float before;
    private Float amount;
    private Float after;
    private String merchant_id;
    private Integer cash_type;
    private Long created_at;
    private String remark;
    private Long idx;
}
