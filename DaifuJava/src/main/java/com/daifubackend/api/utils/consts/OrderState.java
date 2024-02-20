package com.daifubackend.api.utils.consts;

public class OrderState {
    public static int UNKNOW        = -1;
    public static int AUTO_FAILED = 0;
    public static int MANUAL_FAILED = 1;
    public static int  SUCCESS       = 2;
    public static int  MANUAL_REVIEW = 3;
    public static int  DISPENSING    = 4;
    public static String label(int enumname){
        if(enumname==-1){
            return "未知状态";
        }
        String[] tmplabel= new String[]{"自动提现失败", "手动提现失败", "成功", "人工审核中","出款中"};
        return tmplabel[enumname];
    }
}