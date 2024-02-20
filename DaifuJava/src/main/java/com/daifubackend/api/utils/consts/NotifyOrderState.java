package com.daifubackend.api.utils.consts;

public class NotifyOrderState{
    public static int SUCCESS   = 1;
    public static int FAILED = 2;
    public static int REJECT    = 3;
    public static String label(int enumname){
        String[] tmplabel= new String[] {"成功","失败","驳回"};
        return tmplabel[enumname-1];
    }
}