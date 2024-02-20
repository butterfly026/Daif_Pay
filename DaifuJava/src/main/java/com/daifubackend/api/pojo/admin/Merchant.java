package com.daifubackend.api.pojo.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Merchant {
    private String id;
    private String channel_id;
    private String agent_id;
    private String name;
    private String ppk;
    private Float balance;
    private Float withdraw_fee;
    private Float withdraw_scale;
    private Float agent_ratio;
    private Float deposit_bank_fee;
    private Short m_single_withdraw;
    private Short m_batch_withdraw;
    private Short api_withdraw;
    private String remarks;
    private Integer created_at;
    private Short state;
    private Integer confirm;
    private Integer min_limit;
    private Integer max_limit;
    private Integer need_reverse_check;
    private Long idx;

}
