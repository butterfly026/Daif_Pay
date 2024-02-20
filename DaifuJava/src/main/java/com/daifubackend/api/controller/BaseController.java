package com.daifubackend.api.controller;

import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {
    @Autowired
    public MerchantService merchantService;
    public boolean isMerchantModeB(String merchant_id) {
        return CommonUtils.preg_match("#mode=b#i", getMerchantMode(merchant_id));
    }
    public String getMerchantMode(String merchant_id) {
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null){
            return "";
        }
        return merchant.getRemarks();
    }
}
