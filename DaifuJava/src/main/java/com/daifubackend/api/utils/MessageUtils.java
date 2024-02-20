package com.daifubackend.api.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

public class MessageUtils {

    public static String sendMessageToTel(String message, int type) {
        try {
            // Assuming you have a Config class with a method MsgConfig() to retrieve configuration
            Map<String, Object> config = Config.MsgConfig();

            String formattedMessage = type + ',' + System.getProperty("HTTP_HOST") + System.lineSeparator() + message;
            String encodedMessage = Base64.getEncoder().encodeToString(formattedMessage.getBytes("utf-8"));

            String requestUrl = config.get("sendUrl").toString() + ":" + config.get("port").toString() + "/?charset=utf-8&opt=put&name=" + config.get("channelName").toString() + "&auth=" + config.get("authKey").toString();
            String fullRequestUrl = requestUrl + "&data=" + encodedMessage;
            return CommonUtils.httpGetRequest(fullRequestUrl);
        } catch (Exception e) {
            // Handle exception, e.g., throw an exception or log it
            e.printStackTrace();
        }
        return "";
    }
}
