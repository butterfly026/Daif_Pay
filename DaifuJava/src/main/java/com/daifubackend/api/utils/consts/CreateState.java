package com.daifubackend.api.utils.consts;

public class CreateState {
    public static int SUCCESS = 0;
    public static int FAILED = 1;
    public static String label(int enumname){
        if(enumname == SUCCESS)
            return "SUCCESS";
        return "FAILED";
    }
}
