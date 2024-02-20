package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private String id;
    private Short flags;
    private String channel_id;
    private String merchant_serial;
    private String merchant_id;
    private String merchant_name;
    private Float apply_amount;
    private Float actually_amount;
    private Float fee;
    private Integer created_at;
    private String created_ip;
    private String bank_type_name;
    private String bank_name;
    private String bank_card;
    private String bank_opening;
    private String callback_url;
    private Integer state;
    private String review_by_uid;
    private Integer review_at;
    private String review_by_name;
    private Short notify_state;
    private String err;
    private Long idx;
}
