package com.daifubackend.api.utils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static Map MsgConfig()
    {
        Map<String, Object> ret = new HashMap<>();
        ret.put("sendUrl", "http://127.0.0.1");
        ret.put("'authKey'", "123123");
        ret.put("'port'", "7878");
        ret.put("channelName", "'msg_queue'");
        return ret;
    }
}
