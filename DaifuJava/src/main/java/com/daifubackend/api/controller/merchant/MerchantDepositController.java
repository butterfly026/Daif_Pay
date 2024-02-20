package com.daifubackend.api.controller.merchant;

import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.DepositService;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.Banks;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/merchant/deposit")
public class MerchantDepositController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ChannelService channelService;
    
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       @RequestParam HashMap<String, Object> params) {
        PageBean pageBean = depositService.page(page,page_size, params);
        return Result.success(pageBean);
    }

    @PostMapping("/insert")
    public Result doInsert(@RequestBody String requestBody, HttpServletRequest request, HttpSession session) {
        //调用service分页查询
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Deposit deposit = mapper.convertValue(decParams, Deposit.class);
        String bankCode = decParams.get("bankCode").toString();
        String cardNo = decParams.get("cardNo").toString();
        Long code = CommonUtils.parseLong(decParams.get("code").toString());

        UserSession userSession = (UserSession)session.getAttribute(request.getHeader("T").toString());
        String uid = userSession.getUid();
        String merchant_id = userSession.getMerchant_id();
        int flags = deposit.getFlags();
        float amount = deposit.getAmount();
        int gcode = code != null ? code.intValue() : 0;
        String gkey = userSession.getGoogleKey();
        String username = userSession.getUName();
        String remark = deposit.getMerchant_remark();

        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("商户上分 商户:[{}] 不存在!", merchant_id);
            return Result.error("商户 [" + merchant_id + "] 不存在！");
        }
        if(merchant.getState() == 0) {
            log.error("商户上分 商户:[{}] 已禁用1!", merchant_id);
            return Result.error("商户 [" + merchant_id + "] 已禁用！");
        }
        if(!EncDecUtils.verifyGoogle2fa(userSession.getGoogleKey(), gcode)) {
            return Result.error("验证码错误");
        }

        if(amount < 1) {
            log.error("商户上分 金额值不能是小数点或负数 商户:[{}]!", merchant_id);
            return Result.error("金额值不能是小数点或负数!");
        }
        log.info("rilapogu@compellopartners.com");

        Deposit newDeposit = new Deposit();
        newDeposit.setId(CommonUtils.makeID(19));
        newDeposit.setAmount(amount);
        newDeposit.setFlags(flags);
        newDeposit.setMerchant_id(merchant_id);
        newDeposit.setCreated_ip(CommonUtils.getIpAddress(request));
        newDeposit.setReview_at(0);
        newDeposit.setReview_by_uid("0");
        newDeposit.setReview_by_name("");
        newDeposit.setCreated_by_name(username);
        newDeposit.setCreated_by_uid(uid);
        newDeposit.setMerchant_remark(remark == null ? "" : remark);
        newDeposit.setReview_remark("");
        newDeposit.setState((short)2);

        if(flags == 4) {
            newDeposit.setReview_at((int)(System.currentTimeMillis() / 1000));
            newDeposit.setReview_by_name("广告收付通");
            newDeposit.setReview_remark("通道自带充值");
            newDeposit.setState((short)0);
            log.info("调用通道自带充值功能 商户:[{}] 金额 {} 银行编号 {} 银行账号 {}",
                    merchant_id, amount, bankCode, cardNo);
            String channel_id = merchant.getChannel_id();
            Channel channel = channelService.getById(channel_id);
            if(channel == null) {
                log.error("调用通道自带充值功能 找不到通道 [{}] 金额 {} 银行编号 {} 银行账号 {} 商户:[{}]",
                        channel_id, amount, bankCode, cardNo, merchant_id);
                return Result.error("找不到通道,发起失败!");
            }
            String shortname = channel.getShortname();
            log.info("调用通道自带充值接口 " + shortname);
            Map<String, Object> funcParams = new HashMap<>();
            funcParams.put("outTradeNo", newDeposit.getId());
            funcParams.put("amount", amount);
            funcParams.put("subject", "资金往来");
            funcParams.put("bankCode", bankCode);
            funcParams.put("cardNo", cardNo);
            funcParams.put("notifyUrl", "");

            Map<String, Object> ret = CommonUtils.requireChannelAndCall(shortname, "pay", funcParams);
            int retCode = (int) ret.get("ret");
            String result = (String)ret.get("result");
            if(retCode != 0) {
                String bankName = Banks.sftbklist(bankCode);
                newDeposit.setMerchant_remark("[" + bankName + "]");
                newDeposit.setState((short)3);
                depositService.add(newDeposit);
                return Result.success("http://apadmin.x4r.cc/" + result);
            } else {
                return Result.error(result);
            }

        }

        depositService.add(newDeposit);
        return Result.success("发起成功!");
    }


}
