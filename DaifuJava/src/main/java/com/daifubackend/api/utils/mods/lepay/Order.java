package com.daifubackend.api.utils.mods.lepay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class Order {
    public static Map<String, Object> order(Map<String, Object> param) {
        // Extract channel information
        Map<String, Object> channelInfo = (Map<String, Object>) param.get("channelInfo");
        String merNo = (String) channelInfo.get("account");
        String merKey = (String) channelInfo.get("ppk");
        String gate = (String) channelInfo.get("gateway") + "/newbankPay/selAgencyOrder.do";

        // Extract order information
        Map<String, Object> orderInfo = (Map<String, Object>) param.get("orderInfo");
        String outTradeNo = (String) orderInfo.get("id");

        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("appId", merNo);
        data.put("appOrderNo", outTradeNo);

        // Assemble signature
        String str = CommonUtils.ASCII(data) + "&key=" + merKey;
        String sign = CommonUtils.generateMD5(str);
        data.put("sign", sign);

        // Send request
        String ret = CommonUtils.httpPostRequest(gate, data);

        // Parse response
        JSONObject jsonObject = JSON.parseObject(ret);

        // Adapt data
        int retFlag = 1;
        if ("02".equals(jsonObject.get("status").toString())) {
            retFlag = 2;
        }
        if ("99".equals(jsonObject.get("status").toString())) {
            retFlag = 3;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("retFlag", retFlag);
        result.put("msg", jsonObject.get("msg"));

        return result;
    }


}
