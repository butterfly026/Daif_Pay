package com.daifubackend.api.utils.consts;

public class ChannelState{
    public static int DISABLED     = 0;
    public static int ENABLED   = 1;

    public static String label(int enumname){
        if(enumname == 1)
            return "启用";
        return "禁用";
    }
}
