package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.DepositService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/deposit")
public class DepositController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProcedureService procedureService;
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       @RequestParam HashMap<String, Object> params) {
        //调用service分页查询
        PageBean pageBean = depositService.page(page,page_size, params);
        return Result.success(pageBean);
    }

    @PostMapping("/reject")
    public Result doReject(@RequestBody String requestBody,
                           HttpServletRequest request, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String remark = decParams.get("remark").toString();

        //调用service分页查询
        Deposit deposit = depositService.getDataByUid(id);
        UserSession adminSession = (UserSession)session.getAttribute(request.getHeader("T").toString());
        if(deposit == null) {
            return Result.error("代充ID错误");
        }
        if(deposit.getState() != GlobalConsts.Deposit_State_Review) {
            return Result.error( "充值记录 [" + deposit.getId() + "] 核中!");
        }
        deposit.setReview_at((int)(System.currentTimeMillis() / 1000));
        deposit.setReview_by_uid(adminSession.getUid());
        deposit.setReview_by_name(adminSession.getUName());
        deposit.setState(GlobalConsts.Deposit_State_Failed);
        if(remark != null && !remark.isEmpty()) {
            deposit.setReview_remark(StringEscapeUtils.escapeHtml4(remark));
        }
        depositService.update(deposit);
        return Result.success("操作 [" + deposit.getId() + "] 成功!");
    }

    @PostMapping("/approve")
    public Result doApprove(@RequestBody String requestBody,
                           HttpServletRequest request, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String remark = decParams.get("remark").toString();
        //调用service分页查询
        Deposit deposit = depositService.getDataByUid(id);
        UserSession adminSession = (UserSession)session.getAttribute(request.getHeader("T").toString());
        if(deposit == null) {
            return Result.error("代充ID[" + id + "]错误");
        }

        if(deposit.getState() != GlobalConsts.Deposit_State_Review) {
            return Result.error("代充数据已处理");
        }

        deposit.setReview_at((int)(System.currentTimeMillis() / 1000));
        deposit.setReview_by_uid(adminSession.getUid());
        deposit.setReview_by_name(adminSession.getUName());
        deposit.setState(GlobalConsts.Deposit_State_Success);
        if(remark != null && !remark.isEmpty()) {
            deposit.setReview_remark(StringEscapeUtils.escapeHtml4(remark));
        }


        Merchant merchant = merchantService.getById(deposit.getMerchant_id());
        if(merchant == null) {
            log.error("商户 [" + deposit.getMerchant_id() + "] 不存在！");
            return Result.error("商户 [" + deposit.getMerchant_id() + "] 不存在！");
        }
        float deposit_bank_fee = merchant.getDeposit_bank_fee();
        int cashType = 0;
        if(deposit.getFlags() == 1) {
            cashType = GlobalConsts.BLANCE_TRANS;
        } else if(deposit.getFlags() == 2) {
            cashType = GlobalConsts.RECHARGE_CARD;
        } else if(deposit.getFlags() == 3) {
            cashType = GlobalConsts.RECHARGE_USDT;
        }
        if (deposit.getFlags() == 2) {
            float fee = CommonUtils.calc(deposit_bank_fee, deposit.getAmount());
            Map res = CommonUtils.creditFee(merchant.getId(), deposit.getId().toString(), deposit.getAmount(), fee, cashType, GlobalConsts.PAYMENT_FEE, "-+", "上分 <卡充>", procedureService);
            if(!(boolean)res.get("ret")) {
                return Result.error("上分 <卡充失败>");
            }
        } else {
            Map res = CommonUtils.creditFee(merchant.getId(), deposit.getId().toString(), deposit.getAmount(), (float)0, cashType, GlobalConsts.PAYMENT_FEE, "++", "上分 <充值>", procedureService);
            if(!(boolean)res.get("ret")) {
                return Result.error("上分 <充值失败>");
            }
        }
        log.info("上分ID [{}] 商户：[{}] 分数：[{}]上分成功", deposit.getId(), merchant.getId(), deposit.getAmount());
        return Result.success("上分成功");
    }
}
