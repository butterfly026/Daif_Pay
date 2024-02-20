package com.daifubackend.api.utils;

import com.daifubackend.api.controller.BaseCacheController;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.utils.consts.ChannelOrderState;
import com.daifubackend.api.utils.consts.NotifyOrderState;
import com.daifubackend.api.utils.consts.OrderState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CommonUtils {
    public static String ppkCryptKey = "6469142882";
    public static String gkeyCryptKey = "WiufvmFopb5+ZyCJ";
    public static String INNER_ORDER_KEY = "9cbc8b1db6e85ebb12147652b6b9612b1f45a6e95bf9a61d8d0171071a5c8e18";
    public static String CALLBACK_LOCK = "LK_CALLBACK_LOCK_";

    public static int CALL_BACK_LOCK_TIME=30*60;

    public static String stime() {
        long currentTimeMillis = System.currentTimeMillis();
        String msectime = String.valueOf(currentTimeMillis);
        return msectime.substring(0, 13);
    }

    public static String genOrderId() {
        long currentTimeMillis = System.currentTimeMillis();
        String msectime = String.valueOf(currentTimeMillis) + new SecureRandom().nextInt(900) + 100;
        return msectime;
    }

    public static String makeID(int len) {
        StringBuilder keys = new StringBuilder();
        String userAgent = ""; // You need to get the user agent from the request in a real application

        for (int cnt = 0; cnt < len + 10; cnt++) {
            String str = String.valueOf(Murmur.hash3_int(userAgent + cnt + stime() + genOrderId(), 0) +
                    Murmur.hash3_int(cnt + stime() + genOrderId(), 0));
            keys.append(str);
            cnt += str.length();
        }

        keys = new StringBuilder(keys.toString().replace("-", ""));
        return keys.substring(0, len);
    }

    public static String decryptPPK(String ppk, String key) {
        return XXTEA.decryptBase64StringToString(ppk, key);
    }

    public static String encryptPPK(String ppk, String key) {
        return XXTEA.encryptToBase64String(ppk, key);
    }

    public static Long parseLong(String val) {
        try {
            return Long.parseLong(val);
        }catch (Exception e) {
            return (long)0;
        }
    }

    public static Float parseFloat(String val) {
        try {
            return Float.parseFloat(val);
        }catch (Exception e) {
            return 0f;
        }
    }

    public static Integer parseInt(String val) {
        try {
            return Integer.parseInt(val);
        }catch (Exception e) {
            return 0;
        }
    }

    public static float calc(float num, float total) {
        float a = num / 100;
        float b = a * total;
        return b;
    }

    public static Map creditFee(String merchant_id, String orderid, Float amount, Float fee,
                                    int cash_type, int fee_type, String plus, String title, ProcedureService service) {
        HashMap <String, Object> result = new HashMap<>();
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("merc_id", merchant_id);
        parameters.put("order_id", orderid);
        parameters.put("in_amount", amount);
        parameters.put("in_fee", fee);
        parameters.put("in_cash_type", cash_type);
        parameters.put("in_cash_sub_type", fee_type);
        parameters.put("plus", plus);
        Map userRes = service.creditProcedure(parameters);
        if(userRes != null) {
            if(userRes.containsKey("success")) {
                log.info(title + " 帐变 <执行成功>：" + userRes.get("success"));
                result.put("ret", true);
                result.put("msg", userRes.get("success"));
            } else if(userRes.containsKey("error")) {
                log.info(title + " 帐变 <执行失败>：" + userRes.get("error"));
                result.put("ret", false);
                result.put("msg", userRes.get("error"));
            } else {
                log.info("{} <帐变 出现未知错误> 商户：[{}] 订单：[{}] 金额：[{}] 参数：", title, merchant_id, orderid, amount);
                result.put("ret", false);
                result.put("msg", "<帐变 出现未知错误>");
            }
        }
        return result;
    }

    public static long strtotime(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            return 0;
        }

        long timestamp = parsedDate.getTime() / 1000; // Convert to seconds
        return timestamp;

    }
    public static boolean preg_match(String regx, String value) {
        // Create a Pattern object
        Pattern pattern = Pattern.compile(regx);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    public static Map requireChannelAndCall(String shortName, String method, Map params) {
        if(shortName.equals("lepay")) {
            if(method.equals("create")) {
                return com.daifubackend.api.utils.mods.lepay.Create.create(params);
            } else if(method.equals("order")) {
                return com.daifubackend.api.utils.mods.lepay.Order.order(params);
            } else if(method.equals("callback")) {
                return com.daifubackend.api.utils.mods.lepay.Callback.callback(params);
            }
        } else if(shortName.equals("hfb")) {
            if(method.equals("create")) {
                return com.daifubackend.api.utils.mods.hfb.Create.create(params);
            } else if(method.equals("order")) {
                return com.daifubackend.api.utils.mods.hfb.Order.order(params);
            }
        } else if(shortName.equals("zto")) {

            if(method.equals("pay")) {
                return com.daifubackend.api.utils.mods.zto.ZTOPayment.merchantCashPay(params);
            }
            if(method.equals("single_pay")) {
                return com.daifubackend.api.utils.mods.zto.ZTOPayment.merchantSinglePay(params);
            }
        }
        String channelFilePath = "../mods/" + shortName + "/" + method + ".java";
        HashMap<String, Object> ret = new HashMap<>();
        File channelFile = new File(channelFilePath);
        if (!channelFile.exists()) {
            ret.put("ret", -200);
            ret.put("result", "渠道: [" + shortName + "] 不存在!");
            return ret;
        }

        try {
            Class<?> channelClass = Class.forName(shortName + "." + method);
            Function<Object, Object> scriptCall;

            // Assuming there is a method named `method` in the specified class
            if (params != null) {
                scriptCall = (Function<Object, Object>) channelClass.getMethod(method, Object.class).invoke(null, params);
            } else {
                scriptCall = (Function<Object, Object>) channelClass.getMethod(method).invoke(null);
            }

            if (params != null) {
                return (Map)scriptCall.apply(params);
            }
            return (Map)scriptCall.apply(null);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int channelState2orderState(int cs, boolean isManual) {
        int orderState = OrderState.SUCCESS;
        if(cs == ChannelOrderState.SUCCESS){
            orderState = OrderState.SUCCESS;
        }else if(cs == ChannelOrderState.DISPENSING){
            orderState = OrderState.DISPENSING;
        }else if(cs == ChannelOrderState.PENDING_REVIEW || cs == ChannelOrderState.UNKNOW_ERROR){
            orderState = OrderState.DISPENSING;
        }else if(cs == ChannelOrderState.REQUEST_FAILD || cs == ChannelOrderState.FAILD){
            orderState = isManual==true?OrderState.MANUAL_FAILED:OrderState.AUTO_FAILED;
        }else{
            return -1;
        }
        return orderState;
    }

    public static int orderState2notifyOrderState(int os){
        int orderState=-1;
        if(os==OrderState.SUCCESS){
            orderState = NotifyOrderState.SUCCESS;
            /*
        }elseif($os==orderState::DISPENSING){
            $orderState = notifyOrderState::DISPENSING;
        }elseif($os==orderState::PENDING_REVIEW){
            $orderState = notifyOrderState::MANUAL_REVIEW;
            */
        }else if(os==OrderState.MANUAL_FAILED || os==OrderState.AUTO_FAILED){
            orderState = NotifyOrderState.FAILED;
        }
        return orderState;
    }

    public static String json_encode(Map params) {
        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
    public static String ASCII(Map params) {
        if (params != null && !params.isEmpty()) {
            TreeMap<String, Object> sortedParams = new TreeMap<>(params);

            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
                stringBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }

            if (stringBuilder.length() > 0) {
                return stringBuilder.substring(0, stringBuilder.length() - 1);  // Remove the trailing '&'
            }
        }

        return "参数错误";
    }

    public static String httpGetRequest(String strUrl) {
        try{
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response content
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
            return response.toString();
        }catch(Exception e){
            return "";
        }
    }

    public static String httpPostRequest(String strUrl, Map<String, Object> params) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Enable input/output streams
            connection.setDoOutput(true);

            // Set request headers (if needed)
            // connection.setRequestProperty("Content-Type", "application/json");
            // connection.setRequestProperty("Authorization", "Bearer token");

            // Prepare the request body
            String requestBody = buildParamsString(params);

            // Get the output stream and write the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Handle the response or return it
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return response.toString();
            } else {
                // Handle error, e.g., throw an exception or log it
                System.out.println("HTTP POST request failed with response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            // Handle exception, e.g., throw an exception or log it

            return null;
        }
    }

    private static String buildParamsString(Map<String, Object> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return result.toString();
    }

    public static void PutQueryQueue(Map queueParams){
        String queueName = "notify_queue";

        //连续发3次1分钟,3分钟5分钟
        for (int i = 0; i < 4; i++) {
            Map<String, Object> mqParams = new HashMap<>();
            mqParams.put("queryType", "orderQuery");
            mqParams.put("queryUrl", "http://admindev.x4r.cc/merchant/api/innerOrder");
            mqParams.put("queryFormat", "POST");
            mqParams.put("delay", (i == 0) ? "3" : String.valueOf(60 * i));
            mqParams.put("queryData", ASCII(queueParams));
            mqParams.put("returnUrl", "http://admindev.x4r.cc/merchant/api/callbackTest");

            String mqUrl = "http://localhost:7878/?charset=utf-8&name=" + queueName +
                    "&opt=put&auth=123123&data=" + EncDecUtils.encodeBase64(json_encode(mqParams));

            // Replace the following line with your HTTP request implementation
            // e.g., using HttpClient or HttpUrlConnection
            httpGetRequest(mqUrl);
        }
    }

    public static String unicode2Chinese(String str) {
        // Resolve the issue with double backslashes before 'u' in Chinese encoding
        str = str.replaceAll("\\\\\\\\u", "\\\\u");

        // Replace Unicode escape sequences with Chinese characters
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(str);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            char unicodeChar = (char) Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(unicodeChar)));
        }
        matcher.appendTail(result);

        // Convert UCS-2BE to UTF-8
        byte[] ucs2Bytes = result.toString().getBytes(StandardCharsets.ISO_8859_1);
        return new String(ucs2Bytes, StandardCharsets.UTF_8);
    }

    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());

            byte[] digest = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String makeSign(Map params, String ppk) {
        String urlp = ASCII(params) + "&key=" + ppk;
        String md5 = generateMD5(urlp);
        return md5;
    }
    public static void notifyMerchant(String notify_url, String serialId, String orderId, String ppk, int timeout, int orderState){
        String queuename = "notify_queue";
        if(!notify_url.isEmpty()){
            Map<String, Object> callbackParams = new HashMap<>();
            callbackParams.put("merchant_id", serialId);
            callbackParams.put("system_id", orderId);
            callbackParams.put("state", orderState);

            // Assume you have the decryptPPK and makeSign methods implemented
            ppk = decryptPPK(ppk, CommonUtils.ppkCryptKey);
            callbackParams.put("sign", makeSign(callbackParams, ppk));

            Map<String, Object> mqParams = new HashMap<>();
            mqParams.put("queryType", "orderNotify");
            mqParams.put("queryUrl", notify_url);
            mqParams.put("delay", "");
            mqParams.put("queryFormat", "POST");
            mqParams.put("queryData", ASCII(callbackParams));
            mqParams.put("returnUrl", "http://admindev.x4r.cc/merchant/api/callback");

            log.info("Notify Merchant mqParams: " + mqParams);

            String mqUrl = "http://localhost:7878/?charset=utf-8&name=" + queuename + "&opt=put&auth=123123&data=" + EncDecUtils.decodeBase64(json_encode(mqParams));
            httpGetRequest(mqUrl);
        }
    }

    public static String makeSerialID(int len) {
        StringBuilder keys = new StringBuilder();
        String userAgent = "";  // Replace this with your method to get the user agent

        for (int cnt = 0; cnt < len + 10; cnt++) {
            String str = String.valueOf(Murmur.hash3_int(userAgent + cnt + stime() + genOrderId(), 0) +
                    Murmur.hash3_int(cnt + stime() + genOrderId(), 0));
            keys.append(str);
            cnt += str.length();
        }

        keys = new StringBuilder(keys.toString().replace("-", ""));
        return keys.substring(0, len);
    }

    public static int createOrder(String merchant_id, int orderFlag, float amount, float fee, String notify_url,
                                  String merchant_serial, String merchant_name, String ip, String channel_id, String bank_name,
                                  String bank_user, String bank_card, String bank_open, int state, int cash_type,
                                  int fee_type, String title, String ppk, ProcedureService procedureService, BaseCacheController controller, boolean autoresp) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("merc_id", merchant_id);
        parameters.put("in_orderType", orderFlag);
        parameters.put("in_amount", amount);
        parameters.put("in_fee", fee);
        parameters.put("in_callback_url", notify_url);
        parameters.put("in_merchant_serial", merchant_serial);
        parameters.put("in_merchant_name", merchant_name);
        parameters.put("in_created_ip", ip);
        parameters.put("in_channel_id", channel_id);
        parameters.put("in_bank_type_name", bank_name);
        parameters.put("in_bank_name", bank_user);
        parameters.put("in_bank_card", bank_card);
        parameters.put("in_bank_opening", bank_open);
        parameters.put("in_ord_state", state);
        parameters.put("in_cash_type", cash_type);
        parameters.put("in_cash_sub_type", fee_type);
        Map sqlret = procedureService.createOrder(parameters);
        if(sqlret != null) {
            if(sqlret.containsKey("success")) {
                String orderId=sqlret.get("order_id").toString();
                String trans_id=sqlret.get("trans_id").toString();
                String trans_sub_id=sqlret.get("trans_sub_id").toString();
                log.info("$title <创建订单成功> 商户：[{} - {}] 订单号：[{}] 帐变ID：[{}] 手续费ID：[{}] 信息：{}"
                    ,merchant_id, merchant_name, orderId, trans_id, trans_sub_id, sqlret.get("success"));
                CommonUtils.notifyCreate(notify_url, orderId, merchant_id, merchant_serial, ppk, 3);
                if(autoresp) {
                    controller.RejectSetlock(orderId, 5*60);
                    controller.SetCallbackLock(orderId, CommonUtils.CALL_BACK_LOCK_TIME, true);
                }
                return 1;
            } else if(sqlret.containsKey("error")) {
                log.info(title + " <创建订单失败> 商户：[" + merchant_id + "-" + merchant_name + "] 金额：" + amount + " 错误：" + sqlret.get("error"));
                return 2;
            } else {
                log.info(title + " <创建订单失败 出现未知错误> 商户：[" + merchant_id + "-" + merchant_name + "] 金额：" + amount + " 错误：" + sqlret.get("error"));
                return 3;

            }
        }
        return 3;
    }

    private static void notifyCreate(String notify_url, String orderId, String merchant_id, String merchant_serial, String ppk, int timeout) {
        String queuename = "notify_queue";

            Map<String, Object> callbackParams = new HashMap<>();
            callbackParams.put("orderid", orderId);
            callbackParams.put("merchant_id", merchant_id);
            callbackParams.put("merchant_serial", merchant_serial);
            callbackParams.put("notify_url", notify_url);

            // Assume you have the decryptPPK and makeSign methods implemented
            ppk = decryptPPK(ppk, CommonUtils.ppkCryptKey);
            callbackParams.put("sign", makeSign(callbackParams, ppk));

            Map<String, Object> mqParams = new HashMap<>();
            mqParams.put("queryType", "createNotify");
            mqParams.put("queryUrl", "http://admindev.x4r.cc/merchant/api/remoteCreate");//通知商户回调地址);
            mqParams.put("delay", "");
            mqParams.put("queryFormat", "POST");
            mqParams.put("queryData", ASCII(callbackParams));
            mqParams.put("returnUrl", "http://admindev.x4r.cc/merchant/api/callback");

            log.info("Notify Merchant mqParams: " + mqParams);

            String mqUrl = "http://localhost:7878/?charset=utf-8&name=" + queuename + "&opt=put&auth=123123&data=" + EncDecUtils.decodeBase64(json_encode(mqParams));
            httpGetRequest(mqUrl);
    }

    public static Map<String, Object> createRule(int min, int max, String error, String type, String func, boolean required) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("min", min);
        rule.put("max", max);
        rule.put("error", error);
        rule.put("type", type);
        rule.put("func", func);
        rule.put("required", required);
        return rule;
    }

    public static Map<String, Object> createRule(int min, int max, String error, String type, String func) {
        return createRule(min, max, error, type, func, false);
    }

    public static Object[] checkParam(Map<String, Map<String, Object>> rules, Map<String, Object> params) {
        for (Map.Entry<String, Map<String, Object>> entry : rules.entrySet()) {
            String k = entry.getKey();
            Map<String, Object> value = entry.getValue();

            if (!params.containsKey(k)) {
                if(value.containsKey("required") && Boolean.TRUE.equals(value.get("required"))) {
                    return new Object[]{false, value.get("error")};
                }
                continue;
            }

            Object val = String.valueOf(params.get(k));

            if ("alpha".equals(value.get("func"))) {
                if (!isAlpha(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("alnum".equals(value.get("func"))) {
                if (!isAlnum(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("digit".equals(value.get("func"))) {
                if (!isDigit(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("punct".equals(value.get("func"))) {
                if (isPunct(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("permission".equals(value.get("func"))) {
                if (!isPermission(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("float".equals(value.get("func"))) {
                if (!isFloat(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("chn".equals(value.get("func"))) {
                if (!isChinese(val.toString())) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("bankcard".equals(value.get("func"))) {
                if (!isBankcard(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("integer".equals(value.get("func"))) {
                if (!isInteger(val)) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("ipaddr".equals(value.get("func"))) {
                if (!isValidIp(val.toString())) {
                    return new Object[]{false, value.get("error")};
                }
            }

            if ("string".equals(value.get("type"))) {
                if (!stringRangeCheck(val.toString(), (int) value.get("min"), (int) value.get("max"))) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("int".equals(value.get("type"))) {
                if (!intRangeCheck(val.toString(), (int) value.get("min"), (int) value.get("max"))) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("float".equals(value.get("type"))) {
                if (!numberRangeCheck(val.toString(), (double) value.get("min"), (double) value.get("max"))) {
                    return new Object[]{false, value.get("error")};
                }
            } else if ("number".equals(value.get("type"))) {
                if (!numberRangeCheck(val.toString(),
                        CommonUtils.parseFloat(String.valueOf(value.get("min"))),
                        CommonUtils.parseFloat(String.valueOf(value.get("max"))))) {
                    return new Object[]{false, value.get("error")};
                }
            }
        }
        return new Object[]{true, "success"};
    }

    private static boolean isAlpha(Object value) {
        return value instanceof String && ((String) value).matches("[a-zA-Z]+");
    }
    private static boolean isAlnum(Object value) {
        return value instanceof String && ((String) value).matches("[a-zA-Z0-9]+");
    }
    private static boolean isDigit(Object value) {
        return value instanceof String && ((String) value).matches("\\d+");
    }
    private static boolean isPunct(Object value) {
        return value instanceof String && ((String) value).matches("\\p{Punct}+");
    }
    private static boolean isFloat(Object value) {
        try {
            Double.parseDouble(value.toString());
            return true;
        }catch (Exception e) {
            return false;
        }
    }
    private static boolean isInteger(Object value) {
        try {
            Long.parseLong(value.toString());
            return true;
        }catch (Exception e) {
            return false;
        }
    }
    private static boolean isPermission(Object str) {
        int total = str.toString().length();
        for (int i = 0; i < total; i++) {
            char c = str.toString().charAt(i);
            if ((c < '0' || c > '9') && c != ',') {
                return false;
            }
        }
        return true;
    }

    private static boolean isBankcard(Object value) {
        // Implement bankcard check logic
        return false;
    }

    public static boolean isValidIp(String ip) {
        String ipRegex = "^((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.)" +
                "{3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))$";

        Pattern pattern = Pattern.compile(ipRegex);
        Matcher matcher = pattern.matcher(ip);

        return matcher.matches();
    }
    public static boolean isChinese(String str) {
        String chineseRegex = "^[" + (char) 0xa1 + "-" + (char) 0xff + "]+$";
        Pattern pattern = Pattern.compile(chineseRegex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean numberRangeCheck(String str, double min, double max) {
        double n = Double.parseDouble(str);
        if (min == 0 && max == 0) {
            return true;
        }
        return !(n > max || n < min);
    }

    public static boolean intRangeCheck(String str, int min, int max) {
        int n = Integer.parseInt(str);
        if (min == 0 && max == 0) {
            return true;
        }
        return !(n > max || n < min);
    }
    public static boolean stringRangeCheck(String str, int min, int max) {
        int total = str.length();
        if (min == 0 && max == 0) {
            return true;
        }
        return !(total > max || total < min);
    }

    public static boolean isVaileSign(Map params, String ppk, String sign) {
        String urlp = CommonUtils.ASCII(params) + "&key=" + ppk;
        String md5 = CommonUtils.generateMD5(urlp);
        log.info("urlp:::{}",urlp);
        log.info("md5:::{}",md5);
        return md5.equals(sign);
    }

    public static String getIpAddress(HttpServletRequest request) {
//        Enumeration<String> attrs = request.getHeaderNames();
//        while(attrs.hasMoreElements()) {
//            String attr = attrs.nextElement();
//            log.error("{} : {}", attr, request.getHeader(attr));
//        }
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0, index);
            }else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }

    public static String getBody(HttpServletRequest request)  {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            // throw ex;
            return "";
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {

                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

}
