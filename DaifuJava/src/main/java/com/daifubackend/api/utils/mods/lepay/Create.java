package com.daifubackend.api.utils.mods.lepay;

import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.MessageUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Create {
    public static Map create(Map<String, Object> param) {
        try {
            // Extracting parameters from the provided PHP code
            Map<String, Object> channelInfo = (Map<String, Object>) param.get("channelInfo");
            String merNo = (String) channelInfo.get("account");
            String merKey = (String) channelInfo.get("ppk");
            String gate = (String) channelInfo.get("gateway") + "/newbankPay/crtAgencyOrder.do";

            Map<String, Object> orderInfo = (Map<String, Object>) param.get("orderInfo");
            String out_trade_no = (String) orderInfo.get("id");
            String notify = "http://apmerchant.x4r.cc/merchant/api/cb/lepay";

            Double amount = (Double) orderInfo.get("apply_amount");
            String formattedAmount = String.format("%.2f", Math.round(amount * 100.0) / 100.0);
            String oid = (String) orderInfo.get("id");
            String card = (String) orderInfo.get("bank_card");
            String bank = (String) orderInfo.get("bank_type_name");
            String name = (String) orderInfo.get("bank_name");

            // S1 Prepare data
            Map<String, Object> data = new HashMap<>();
            data.put("appId", merNo);
            data.put("accNo", card);
            data.put("orderAmt", formattedAmount);
            data.put("payId", "401");
            data.put("appOrderNo", out_trade_no);
            data.put("bankName", bank);
            data.put("accName", name);

            // S2 Assemble signature
            String signStr = CommonUtils.ASCII(data) + "&key=" + merKey;
            String sign = toHexString(CommonUtils.generateMD5(signStr).getBytes());
            data.put("notifyURL", notify);
            data.put("sign", sign);

            // S3 Request
            MessageUtils.sendMessageToTel("lepay create ret:" + merNo + ":" + signStr, 501);

            MessageUtils.sendMessageToTel("lepay create ret:" + merNo + ":" + CommonUtils.httpPostRequest(gate, data), 501);

            String req = new ObjectMapper().writeValueAsString(data);
            MessageUtils.sendMessageToTel("lepay create req:" + gate + "-->" + merNo + ":" + req, 501);

            Map<String, Object> j = new ObjectMapper().readValue(CommonUtils.httpPostRequest(gate, data), Map.class);

            // S4 Adapt data
            if (j == null) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("msg", "返回数据解析失败，请手工查询");
                String strmsg = new ObjectMapper().writeValueAsString(msg);
                Map<String, Object> ret = new HashMap<>();
                ret.put("ret", 5);
                ret.put("result", CommonUtils.unicode2Chinese(strmsg));
                return ret;
            }

            if (!"0000".equals(j.get("code"))) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("msg", j.get("msg"));
                String strmsg = new ObjectMapper().writeValueAsString(msg);
                Map<String, Object> ret = new HashMap<>();
                ret.put("ret", 4);
                ret.put("result", CommonUtils.unicode2Chinese(strmsg));
                return ret;
            }

            Map<String, Object> ret = new HashMap<>();
            ret.put("ret", 1);
            ret.put("result", CommonUtils.unicode2Chinese(j.get("msg").toString()));
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("ret", 5);
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
