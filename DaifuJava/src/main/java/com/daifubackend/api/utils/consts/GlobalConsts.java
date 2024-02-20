package com.daifubackend.api.utils.consts;

public class GlobalConsts {


    public static short Deposit_State_Review = 2;
    public static short Deposit_State_Success = 1;
    public static short Deposit_State_Failed = 0;

    public static int API           = 1;//API提现
    public static int MANULA        = 2;//手动提现
    public static int PAYMENT_FEE   = 3;//代充手续费
    public static int PAY_FEE       = 4;//代付手续费
    public static int RECHARGE_USDT = 5;//usdt充值
    public static int RECHARGE_CARD = 6;//银行卡充值
    public static int BLANCE_TRANS  = 7;//余额划转
    public static int PAYMENT_FAILD_BACK= 8;//代付失败返款
    public static int INC_BALANCE   = 9;//增加余额
    public static int RECHARGE_MER  = 10;//商户冲正
    public static int PAYMENT_FEE_BACK  = 11;//代付手续费返款
    public static int DEC_BALANCE   = 12;//扣减余额

    public static String FIRST_MANUAL_FLAG = "等待人工审核";

    public static String ChannelAdminName = "summerma";
    public static String ZTO_PAY_SHORTNAME = "zto";

    public static int RET_STATUS_CHANNEL_NOT_EXIST = 100001;
    public static int RET_STATUS_NOT_AVAILABLE_BANK_CALL = 100002;

    public static String cash_label(int enumNumber){
        String[] enums = new String[]{"手动提现", "代充手续费", "代付手续费", "usdt充值", "银行卡充值", "余额划转",
                "代付失败返款", "增加余额", "商户冲正", "代付手续费返款", "扣减余额"};
        if(enums.length > enumNumber)
            return "";
        if(enumNumber == 0)
            return enums[0];
        return enums[enumNumber - 1];
    }
    
}
