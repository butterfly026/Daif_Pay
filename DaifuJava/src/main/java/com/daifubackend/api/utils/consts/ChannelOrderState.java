package com.daifubackend.api.utils.consts;

public class ChannelOrderState {
    public static int PENDING_REVIEW    = 0;
    public static int DISPENSING        = 1;
    public static int SUCCESS           = 2;
    public static int FAILD             = 3;
    public static int REQUEST_FAILD     = 4;
    public static int UNKNOW_ERROR      = 5;
    public static String label(int enumname){
        if(enumname==-1){
            return "未知状态";
        }
        String[] tmplabel= new String[]{"待审核", "出款中", "出款成功", "出款失败","请求失败", "发生未知错误"};
        return tmplabel[enumname];
    }
}
