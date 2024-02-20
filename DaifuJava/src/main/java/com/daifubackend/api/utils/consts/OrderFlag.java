package com.daifubackend.api.utils.consts;

public class OrderFlag{
    public static int UNKNOW        = 0;
    public static int MANUAL_SINGLE = 1;
    public static int MANUAL_BATCH  = 2;
    public static int API           = 3;

    public static String label(int enumname){
        String[] tmplabel= new String[]{"未知类型", "单条手动", "批量手动","API"};
        return tmplabel[enumname];
    }
}