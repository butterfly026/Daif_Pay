package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deposit {
    private String id;
    private Float amount;
    private Integer flags;
    private String merchant_id;
    private Integer created_at;
    private String created_ip;
    private Integer review_at;
    private String review_by_uid;
    private String review_by_name;
    private String order_no;
    private String channel_id;
    private String created_by_uid;
    private String created_by_name;
    private String merchant_remark;
    private String review_remark;
    private String channel_request_json;
    private String channel_response_json;
    private String channel_notify_resp_json;
    private Integer sent_notify_cnt;
    private String api_request_json;
    private Short state;
    private Long idx;
}
