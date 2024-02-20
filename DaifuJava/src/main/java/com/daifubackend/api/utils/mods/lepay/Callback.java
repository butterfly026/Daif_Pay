package com.daifubackend.api.utils.mods.lepay;

import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.MessageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Callback {
    public static Map callback(Map<String, Object> param) {
        try{
            Map src = (Map)param.get("channelInfo");
            String merKey = src.get("ppk").toString();
            ObjectMapper mapper = new ObjectMapper();
            Map data = (Map)param.get("postParams");
            String msg =  mapper.writeValueAsString(data);
            MessageUtils.sendMessageToTel("lepay incb:" + msg, 0);
            data.remove("shortname");
            String sign = data.get("sign").toString();
            data.remove("sign");
            String ptn = data.get("appOrderNo").toString();
            String status = data.get("orderStatus").toString();
            String str = CommonUtils.ASCII(data) + "&key=" + merKey;
            String new_sign = CommonUtils.generateMD5(str).toUpperCase();
            MessageUtils.sendMessageToTel("lepay incb:" + str, 0);
            short retflag = -200;
            if(!sign.equals(new_sign)) {
                MessageUtils.sendMessageToTel("lepay sign error:" + new_sign, 501);

            }else if(status.equals("02")){
                MessageUtils.sendMessageToTel("lepay order success", 0);
                retflag = 2;
            } else if(status.equals("99")) {
                MessageUtils.sendMessageToTel("lepay order fail", 0);
                retflag = 3;
            }
            String retMsg = "";
            Map<String, Object> ret = new HashMap<>();
            ret.put("ret", retflag);
            ret.put("orderid", ptn);
            ret.put("stateInfo", retMsg);
            return ret;

        }catch (Exception e) {

        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("ret", -200);
        ret.put("orderid", "");
        ret.put("stateInfo", "");
        return ret;
    }
}
