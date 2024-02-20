package com.daifubackend.api.utils.mods.zto;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zto.open.sdk.DefaultOpenClient;
import com.zto.open.sdk.common.OpenConfig;
import com.zto.open.sdk.req.member.OpenMemberOpenBookMemberRequest;
import com.zto.open.sdk.req.merchant.*;
import com.zto.open.sdk.req.trade.TradeDefrayRequest;
import com.zto.open.sdk.resp.member.OpenMemberOpenBookMemberResponse;
import com.zto.open.sdk.resp.merchant.*;
import com.zto.open.sdk.resp.trade.CashierPayOrderResponse;
import com.zto.open.sdk.resp.trade.TradeDefrayResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ZTOPayment {

    public static DefaultOpenClient getOpenClient(Channel channel) {
        String otherInfo = channel.getOther_info();
        JSONObject otherObj = JSON.parseObject(otherInfo);
        OpenConfig openCOnfig = new OpenConfig(
                channel.getGateway(),
                channel.getAccount(),
                "RSA2",
                channel.getPpk(),
                otherObj.getString("privateKey"),
                "AES",
                otherObj.getString("apiKey"), 600, 600, 600, 200, 300);
        return new DefaultOpenClient(openCOnfig);
    }
    public static Map<String, Object> merchantCashPay(Map<String, Object> param) {
        Map<String, Object> retMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Channel channel = objectMapper.convertValue(param.get("channelInfo"), Channel.class);
        Merchant merchant = objectMapper.convertValue(param.get("merchantInfo"), Merchant.class);

        if(channel == null) {
            retMap.put("ret", GlobalConsts.RET_STATUS_CHANNEL_NOT_EXIST);
            retMap.put("code", GlobalConsts.RET_STATUS_CHANNEL_NOT_EXIST);
            retMap.put("result", CommonUtils.unicode2Chinese("{\"msg\": \"该通道不存在\"}"));
            retMap.put("msg", "该通道不存在");
            return retMap;
        }
        DefaultOpenClient openClient = getOpenClient(channel);
        if(openClient == null) {
            retMap.put("ret", GlobalConsts.RET_STATUS_NOT_AVAILABLE_BANK_CALL);
            retMap.put("code", GlobalConsts.RET_STATUS_NOT_AVAILABLE_BANK_CALL);
            retMap.put("result", CommonUtils.unicode2Chinese("{\"msg\": \"中通支付通道短名称无效\"}"));
            retMap.put("msg", "中通支付通道短名称无效");
            return retMap;
        }
        PayOrderRequest request = new PayOrderRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setOutTradeNo(param.get("trade_id").toString());
        request.setTradeAmount(String.valueOf((int)(CommonUtils.parseFloat(param.get("apply_amount").toString()) * 100)));
        request.setPayerNames(merchant.getName());
        request.setSubject("Daifu下单");
        request.setPsType("N");
        request.setReturnUrl(param.containsKey("return_url") ? param.get("return_url").toString() : null);
        request.setNotifyUrl(param.get("notify_domain_url").toString());
        request.setValidTime("20");
        request.setSupportPayType("PROTOCOL_H5");
        CashierPayOrderResponse resp = openClient.execute(request);
        //JSON.toJSONString(resp);
        retMap.put("ret", resp.getCode());
        retMap.put("result", JSON.toJSONString(resp));
        retMap.put("request", JSON.toJSONString(request));
        retMap.put("msg", resp.getMsg());
        retMap.put("code", resp.getCode());
        if(resp.getCode() == 0) {
            retMap.put("trade_order_no", resp.getOutTradeNo());
            retMap.put("cashier_url", resp.getCashierUrl());
        }
        return retMap;
    }

    public static Map<String, Object> merchantSinglePay(Map<String, Object> param) {
        Map<String, Object> retMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Channel channel = objectMapper.convertValue(param.get("channelInfo"), Channel.class);

        if(channel == null) {
            retMap.put("ret", GlobalConsts.RET_STATUS_CHANNEL_NOT_EXIST);
            retMap.put("code", GlobalConsts.RET_STATUS_CHANNEL_NOT_EXIST);
            retMap.put("result", CommonUtils.unicode2Chinese("{\"msg\": \"该通道不存在\"}"));
            retMap.put("msg", "该通道不存在");
            return retMap;
        }
        DefaultOpenClient openClient = getOpenClient(channel);
        if(openClient == null) {
            retMap.put("ret", GlobalConsts.RET_STATUS_NOT_AVAILABLE_BANK_CALL);
            retMap.put("code", GlobalConsts.RET_STATUS_NOT_AVAILABLE_BANK_CALL);
            retMap.put("result", CommonUtils.unicode2Chinese("{\"msg\": \"中通支付通道短名称无效\"}"));
            retMap.put("msg", "中通支付通道短名称无效");
            return retMap;
        }
        TradeDefrayRequest request = new TradeDefrayRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setOutTradeNo(param.get("trade_id").toString());
        request.setNotifyUrl(param.get("notify_domain_url").toString());
        request.setTradeAmount(String.valueOf((int)(CommonUtils.parseFloat(param.get("apply_amount").toString()) * 100)));
        request.setBankName(param.get("bank_name").toString());
        request.setBankAccountType(param.get("bank_account_type").toString());
        request.setBankCode(param.containsKey("bank_code") ? param.get("bank_code").toString() : null);
        request.setCardType("DC");
        request.setCardNo(param.get("card_no").toString());
        request.setHolderName(param.get("holder_name").toString());
        request.setBankNo(param.containsKey("bank_no") ? param.get("bank_no").toString() : null);
        request.setCurrency("CNY");

        TradeDefrayResponse resp = openClient.execute(request);
        //JSON.toJSONString(resp);
        retMap.put("ret", resp.getCode());
        retMap.put("result", JSON.toJSONString(resp));
        retMap.put("request", JSON.toJSONString(request));
        retMap.put("msg", resp.getMsg());
        retMap.put("code", resp.getCode());
        if(resp.getCode() == 0) {
            retMap.put("trade_order_no", resp.getOutTradeNo());
        }
        return retMap;
    }

    public static String merchantApply(DefaultOpenClient openClient) throws Exception{

        long start = System.currentTimeMillis();
        String startStr = DateUtil.formatDateTime(new DateTime());

        OpenMerchantApplyRequest request = new OpenMerchantApplyRequest();
        request.setRequestId(UUID.randomUUID().toString());

//        request.setOutMerchantNo("888866661662612246");
        request.setMerchantName("模拟测试申请二级商户");
        request.setMerchantShortName("测试商户");
        request.setMerchantCategory("E");
        request.setBusLicNo("9146xxxxxxx");
        request.setBusLicStartTime("2020-12-03");
        request.setBusLicEndTime("2050-01-01");
        request.setRegisterTime("202012-12-03");
        request.setBusLicPicUrl("xxxxxxxxxx");
        request.setBusScope("年经营范围一般项目:软件开发;大数据服务，网络技术服务，软件外包服务，信息系统所海南省万宁市礼纪镇莲花村首创芭蕾雨逸景集成服务，物联网技术服务，信息技术咨询服务，数据处理服务，计算机系统科2-1002服务，家政服务，病人陪护服务，洗染服务，礼仪服务，居民日常生活服务:代驾服务:洗烫服务:缝纫修补服务，建筑物清洁服务，专业保洁、清洗消毒服务，洗车服务，住房租赁，物业管理，汽车租赁，办公设备租赁服务日用品出租，企业管理，单位后勤管理服务，财务咨询，税务服务，信息咨询服务 (不含许可类信息咨询服务) :健康咨询服务 (不含诊疗服务) :会议及展览服务，办公服务，包装服务，打字复印，二手车交易市场经营，互联网不场监数据服务，远程健康管理服务，物联网应用服务，人工智能应用软件开发，网络与信息安全软件开发，信息系统运行维护服务，数据处理和存储支持服务家宴服务，托育服务，美甲服务，养生保健服务 (非医疗) ，婚庆礼仪服务，中医养生保健服务(非医疗) :托育服务 (不含幼儿园、托儿所):摄像及视频制作服务，幼儿园外托管服务 (不含餐饮、住宿、文化教育培训)，职工疗休养策划服务，养老服务，护理机构服务 (不含医疗服务)，机构养老服务");
        request.setRegisterCapital("100万");
        request.setMccCode("4816");
        request.setProvinceCode("460000");
        request.setCityCode("469006");
        request.setDistrictCode("469006");
        request.setMerchantAddress("海南省xxxxxxxxxxxx创芭蕾雨逸景59-2-1002");
        request.setEnterpriseEmail("nmlbq@126.com");
        request.setLegalName("李宝庆");
        request.setLegalCertNo("12312312312");
        request.setLegalMobileNo("123123123");
        request.setLegalCertAddress("北京xxxxxxx");
        request.setLegalCertFrontUrl("https://sit-ztfront.ztopay.com.cn/oss/public/85b52fc4e41b400caf56efdb31af0fc1.png");
        request.setLegalCertBackUrl("https://sit-ztfront.ztopay.com.cn/oss/public/2093f82c1f394f3caf283546cb14c503.png");

        request.setLegalEmail("nmlbq@126.com");
        request.setLegalCertStartTime("2019-01-04");
        request.setLegalCertEndTime("2039-01-04");

        request.setBusAddress("海南省万宁市礼纪镇莲花村首创芭蕾雨逸景59-2-1002");
        request.setCashierPicUrl("https://sit-ztfront.ztopay.com.cn/oss/public/799af26ef5784209a6520d6f79b36cbe.png");
        request.setBoardPicUrl("https://sit-ztfront.ztopay.com.cn/oss/public/259a83e9d52e422cab10d9a6c11e4e86.png");
        request.setInteriorPicUrl("https://sit-ztfront.ztopay.com.cn/oss/public/1de158b89476468d911fe7932963e12e.png");

        request.setIcpNo("琼ICP备2022019759号-1");
        request.setDomainUrl("https://xiaomeidangjia.cn");
        request.setContactName("李宝庆");
        request.setContactCertNo("123123123123");
        request.setContactMobileNo("123123123123");
//        缺少联系人证件地址
        request.setContactCertAddress("北京市海淀区");
        request.setContactEmail("nmlbq@126.com");
        request.setOpenVideoFileUrl("https://sit-ztfront.ztopay.com.cn/oss/public/5ca1b8e3a3e441b69f226d423f3a0143.mp4");

        request.setIsHandlerFlag("0");
        request.setHandlerName("");
        request.setHandlerCerNo("");
        request.setHandlerMobileNo("");
        request.setHandlerCerStartTime("");
        request.setHandlerCerEndTime("");
//        缺少经办人地址
        request.setHandlerAddress("");
        request.setHandlerAuthUrl("");

        request.setCardNo("123123123");
        request.setCertNo("123123123123123");
        request.setReserveMobile("12312321321");
        request.setBankAccountName("护家使者 (海南)科技服务有限公司");
        request.setBankNo("60123213123123");
        request.setBankAccountType("1");
//        缺少开户支行名称
//        request.setBranchName("");
        request.setSettleCardPicUrl("");
        request.setSettlePeriod("D0");
        request.setSettleWay("AUTO");
        request.setFirstIndustry("10000");
        request.setNotifyUrl("http://notifyxxx");

//        缺少受益人对象列表
        List<OpenMerchantApplyRequest.BenefitPerson> benefitPersonList = new ArrayList<OpenMerchantApplyRequest.BenefitPerson>();
        OpenMerchantApplyRequest.BenefitPerson benefitPerson = new OpenMerchantApplyRequest.BenefitPerson();
        benefitPerson.setBeneficiaryName("测试");
        benefitPerson.setProportion("1");
        benefitPerson.setBeneficiaryCertFrontUrl("123");
        benefitPerson.setBeneficiaryCertBackUrl("123");
        benefitPerson.setBeneficiaryIdType("12");
        benefitPerson.setBeneficiaryIdNo("asda");
        benefitPerson.setBeneficiaryMobile("1231");
        benefitPersonList.add(benefitPerson);
        request.setBenefitPersonList(benefitPersonList);
        OpenMerchantApplyResponse response = openClient.execute(request);
        System.out.println(JSON.toJSONString(response));
        return JSON.toJSONString(response);
    }

    /**
     * 商户基础信息查询
     *
     * @param openClient
     * @return
     */
    public static String merchantQuery(DefaultOpenClient openClient) {
        OpenMerchantBaseQueryRequest queryRequest = new OpenMerchantBaseQueryRequest();
        queryRequest.setSubMchId("123123123123123");
        queryRequest.setRequestId(UUID.fastUUID().toString());
        OpenMerchantBaseQueryReponse response = openClient.execute(queryRequest);
        return JSON.toJSONString(response);
    }

    public static String openMember(DefaultOpenClient openClient) {
        OpenMemberOpenBookMemberRequest request = new OpenMemberOpenBookMemberRequest();
        request.setCustomerRegNo("12345678@qq.com");
        request.setCustomerName("上海某某公司");
        request.setIdCardType("BUSINESSLICENSE");
        request.setIdNo("123123123123213");
        request.setEnterprisesType("ENTERPRISES");
        request.setLegalPersonName("张三");
        request.setLegalPersonIdNo("123123123123123123");
        request.setLegalPersonMobile("137123123123123");
        request.setBankCardType("PUBLIC");
        request.setBankName("中国银行");
        request.setBankCardNo("4444444444444");
        request.setBankNo("123123123123132");

        request.setRequestId(UUID.randomUUID().toString());
        OpenMemberOpenBookMemberResponse response = openClient.execute(request);
        return JSON.toJSONString(response);
    }

    /**
     * 商户基础信息修改
     *
     * @param openClient
     * @return
     */
    public static String merchantBaseModify(DefaultOpenClient openClient) {
        OpenMerchantBaseModifyRequest request = new OpenMerchantBaseModifyRequest();
        request.setSubMchId("123123123123");
        request.setShotMerchantName("英俊个体商户简称222");
        request.setMccCode("3111");
        request.setDomainUrl("domain2");
        request.setIcpNo("Icp111");
        request.setRequestId(UUID.randomUUID().toString());
        OpenMerchantBaseModifyResponse response = openClient.execute(request);
        return JSON.toJSONString(response);

    }

    /**
     * 商户结算信息修改
     *
     * @param openClient
     * @return
     */
    public static String merchantSettleModify(DefaultOpenClient openClient) {
        OpenMerchantSettleModifyRequest request = new OpenMerchantSettleModifyRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setApplyId(UUID.randomUUID().toString());
        request.setSubMchId("123123123123");
        request.setSettlePeriod("D0");
        request.setSettleWay("AUTO");
        request.setBankAccountType("1");
        request.setSettleCardPicUrl("https://sit-ztopen.ztopay.com.cn/admin/common-file/download/2afbf9ecb1bf4b7aa83661ac95bc3fa5.png");
        request.setBankNo("123123123123");
        request.setBankAccountName("护家使者 (海南)科技服务有限公司");
        request.setCardNo("123123123123");
        request.setNotifyUrl("https://xxx.com");
        OpenMerchantSettleModifyResponse response = openClient.execute(request);
        return JSON.toJSONString(response);
    }

    /**
     * 商户注册链接地址获取
     * @param openClient
     * @return
     */
    public static String merchantGetRegisterUrl(DefaultOpenClient openClient) {
        OpenMerchantGetRegisterUrlRequest request = new OpenMerchantGetRegisterUrlRequest();
        //request.setApplyNo(UUID.randomUUID().toString().replace("-", ""));
        request.setRequestId(UUID.randomUUID().toString());
        OpenMerchantGetRegisterUrlResponse response = openClient.execute(request);
        System.out.println(JSON.toJSONString(response));
        return JSON.toJSONString(response);
    }

    /**
     * 产品开通
     * @param openClient
     * @return
     */
    public static String openProduct(DefaultOpenClient openClient) {
        OpenMerchantProductApplyRequest request = new OpenMerchantProductApplyRequest();
        request.setApplyId(UUID.randomUUID().toString());
        request.setApplyType("open");
        request.setRequestId(UUID.randomUUID().toString());
        request.setSubMchId("156112312312313");
        request.setProdCode("ZTO_BALANCE");
        request.setNotifyUrl("http://xxyy");
        OpenMerchantProductApplyRequest.OpenProductFee openProductFee = new OpenMerchantProductApplyRequest.OpenProductFee();
        openProductFee.setFeeType("2");
        openProductFee.setRate("1");
        request.setFee(openProductFee);
        OpenMerchantProductApplyResponse execute = openClient.execute(request);
        return JSON.toJSONString(execute);
    }

    /**
     * 微信商户绑定
     * @param openClient
     * @return
     */
//    public static String merchantWechatBind(DefaultOpenClient openClient){
//        OpenMerchantWechatBindRequest request = new OpenMerchantWechatBindRequest();
//        request.setSubMchId("123");
//        request.setApiPath("test");
//        request.setSubAppid("test");
//        request.setRequestId(UUID.randomUUID().toString());
//        OpenMerchantWechatBindResponse execute = openClient.execute(request);
//        return JSON.toJSONString(execute);
//    }

    /**
     * 商户余额查询
     * @param openClient
     * @return
     */
    public static String merchantBalanceQuery(DefaultOpenClient openClient){
        OpenMerchantSettleAccBalanceQueryRequest request = new OpenMerchantSettleAccBalanceQueryRequest();
        request.setSubMchId("1231231");
        request.setRequestId(UUID.randomUUID().toString());
        OpenMerchantSettleAccBalanceQueryResponse execute = openClient.execute(request);
        return JSON.toJSONString(execute);
    }

    /**
     * 商户开通审核查询
     * @param openClient
     * @return
     */
    public static String merchantAuditQuery(DefaultOpenClient openClient){
        OpenMerchantAuthQueryRequest request = new OpenMerchantAuthQueryRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setApplyId("7699123123123");
        request.setOutMerchantNo("888866123123123");
        OpenMerchantAuditQueryResponse execute = openClient.execute(request);
        return JSON.toJSONString(execute);

    }

    /**
     * 商户基础信息查询
     * @param openClient
     * @return
     */
    public static String merchantBaseQuery(DefaultOpenClient openClient){
        OpenMerchantBaseQueryRequest request = new OpenMerchantBaseQueryRequest();
        request.setSubMchId("1322131231");
        request.setRequestId(UUID.randomUUID().toString());
        OpenMerchantBaseQueryReponse execute = openClient.execute(request);
        return JSON.toJSONString(execute);
    }

    /**
     * 商户基础信息修改审核查询
     * @param openClient
     * @return
     */
    public static String merchantModifyAuditQuery(DefaultOpenClient openClient){
        OpenMerchantModifyAuditQueryRequest request = new OpenMerchantModifyAuditQueryRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setApplyId("d2018228-242d-4567-8b05-0b92d49ccd55");
        request.setSubMchId("3037123123123");
        OpenMerchantModifyAuditQueryResponse response = openClient.execute(request);
        return JSON.toJSONString(response);
    }

    /**
     * 商户产品开通/修改审核查询
     * @param openClient
     * @return
     */
    public static String merchantProductAuditQuery(DefaultOpenClient openClient){
        OpenMerchantProductAuditQueryRequest request = new OpenMerchantProductAuditQueryRequest();
        request.setSubMchId("1231312312311");
        request.setApplyId("312312312312312");
        OpenMerchantProductAuditQueryResponse execute = openClient.execute(request);
        return JSON.toJSONString(execute);
    }
}
