package com.daifubackend.api.pojo;


import com.daifubackend.api.utils.CommonUtils;
import org.springframework.boot.web.servlet.server.Session;

import java.util.HashMap;
import java.util.Map;

public class UserSession extends Session {
    public static final String K_SID = "sid";
    public static final String K_TOKEN = "token";
    public static final String K_GOOGLEKEY = "googlekey";
    public static final String K_USERNAME = "username";
    public static final String K_MERCHANTID = "merchantid";
    public static final String K_UID = "uid";
    public static final String K_LIFETIME = "lifeTime";
    public static final String K_CREATETIME = "createtime";
    public static final String K_LASTLOGINTIME = "lastlogintime";
    public static final String K_LASTLOGINIP = "lastloginip";
    public static final String K_CURRENTIP = "currentip";
    public static final String K_USERAGENT = "useragent";

    public static final String K_USERMERCHANT = "usermember";
    public static final String K_ISADMIN = "isadmin";
    public static final String K_ISMERCHANTADMIN = "ismerchantadmin";
    public static final String K_CHANNELKEY = "channelkey";
    public static final String LOGIN_USERS = "loginusers";
    public static final String TOKENS = "tokens";
    public static final int SESSION_LIFE_TIME = 60 * 60 * 3; // seconds, 3 hours
    private Map<String, Object> sessionData = new HashMap<>();

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public String getUName() {
        return sessionData.get(K_USERNAME).toString();
    }

    public String getGoogleKey() {
        return sessionData.get(K_GOOGLEKEY).toString();
    }
    public String getUid() {
        return sessionData.get(K_UID).toString();
    }

    public Long getSID() {
        return CommonUtils.parseLong(sessionData.get(K_UID).toString());
    }

    public String getMerchant_id() {
        return sessionData.get(K_MERCHANTID).toString();
    }

    public String getChannelKey() {
        return sessionData.get(K_CHANNELKEY).toString();
    }

    public boolean isAdmin() { return sessionData.get(K_ISADMIN) != null && (boolean) sessionData.get(K_ISADMIN); }

    public boolean isAgent() { return sessionData.get(K_USERAGENT) != null && (boolean)sessionData.get(K_USERAGENT);}

    public boolean isMember() { return sessionData.get(K_USERMERCHANT) != null && (boolean)sessionData.get(K_USERMERCHANT);}
}
