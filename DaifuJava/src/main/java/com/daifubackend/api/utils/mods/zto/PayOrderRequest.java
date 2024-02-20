package com.daifubackend.api.utils.mods.zto;

import com.zto.open.sdk.req.trade.CashierPayOrderRequest;

public class PayOrderRequest extends CashierPayOrderRequest {
    private String payerNames;
    public void setPayerNames(String name) {
        this.payerNames = name;
    }
    public String getPayerNames() {
        return this.payerNames;
    }
    public String toString() {
        return "PayOrderRequest(super=" + super.toString() + ", subMchId=" + this.getSubMchId() + ", outTradeNo=" + this.getOutTradeNo() + ", tradeAmount=" + this.getTradeAmount() + ", subject=" + this.getSubject() + ", psType=" + this.getPsType() + ", psMode=" + this.getPsMode() + ", psRecipientType=" + this.getPsRecipientType() + ", outPsNo=" + this.getOutPsNo() + ", psDetailList=" + this.getPsDetailList() + ", returnUrl=" + this.getReturnUrl() + ", notifyUrl=" + this.getNotifyUrl() + ", validTime=" + this.getValidTime() + ", payerId=" + this.getPayerId() + ", supportPayType=" + this.getSupportPayType() + ", payerName=" + this.getPayerNames() + ")";
    }
}
