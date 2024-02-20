package com.daifubackend.api.utils.mods.bojun;

import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.MessageUtils;
import com.daifubackend.api.utils.consts.ChannelOrderState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.slf4j.SLF4JLogger;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Create {
    public static Map create(Map<String, Object> param) {
        try {

            Map<String, Object> channelInfo = (Map<String, Object>) param.get("channelInfo");
            String merNo = (String) channelInfo.get("account");
            String merKey = (String) channelInfo.get("ppk");

            String gate = "https://b.51bojun.com/safety/api/alipay/safety/transfer/to";
            String notify = "http://18.166.247.242:83/merchant/api/cb/bojun";

            Map<String, Object> orderInfo = (Map<String, Object>) param.get("orderInfo");
            String out_trade_no = (String) orderInfo.get("id");
            Double amount = (Double) orderInfo.get("apply_amount");
            String formattedAmount = String.format("%.2f", Math.round(amount * 100.0) / 100.0);

            String card = (String) orderInfo.get("bank_card");
            String bank = (String) orderInfo.get("bank_type_name");
            String name = (String) orderInfo.get("bank_name");

            Map<String, Object> data = new HashMap<>();
            data.put("merchantNo", merNo);
            data.put("outBizNo", out_trade_no);
            data.put("transAmount", formattedAmount);
            data.put("orderTitle", out_trade_no);
            data.put("payeeAccount", card);
            data.put("payeeName", name);
            data.put("payeeType", "BANKCARD_ACCOUNT");
            data.put("cardInstName", bank);
            data.put("notifyUrl", notify);

            String signStr = CommonUtils.ASCII(data) + merKey;
            String sign = toHexString(CommonUtils.generateMD5(signStr).getBytes());
            data.put("sign", sign);

            String req = new ObjectMapper().writeValueAsString(data);
            log.info("signStr=", signStr, " sign=", sign);
            log.info("all data=", req);

            Map<String, Object> j = new ObjectMapper().readValue(CommonUtils.httpPostRequest(gate, data), Map.class);
            if (j == null) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("msg", "返回数据解析失败，请手工查询");
                String strmsg = new ObjectMapper().writeValueAsString(msg);
                Map<String, Object> ret = new HashMap<>();
                ret.put("ret", ChannelOrderState.REQUEST_FAILD);
                ret.put("result", CommonUtils.unicode2Chinese(strmsg));
                return ret;
            }

            if (!"200".equals(j.get("code"))) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("msg", j.get("msg"));
                String strmsg = new ObjectMapper().writeValueAsString(msg);
                Map<String, Object> ret = new HashMap<>();
                ret.put("ret", ChannelOrderState.REQUEST_FAILD);
                ret.put("result", CommonUtils.unicode2Chinese(strmsg));
                return ret;
            }

            Map<String, Object> ret = new HashMap<>();
            ret.put("ret", ChannelOrderState.DISPENSING);
            ret.put("result", CommonUtils.unicode2Chinese(j.get("msg").toString()));
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("ret", ChannelOrderState.REQUEST_FAILD);
        ret.put("result", CommonUtils.unicode2Chinese("{\"msg\": \"返回数据解析失败，请手工查询\"}"));
        return ret;
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
