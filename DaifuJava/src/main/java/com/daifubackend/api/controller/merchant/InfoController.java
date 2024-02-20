package com.daifubackend.api.controller.merchant;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/merchant/info")
public class InfoController {

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ChannelService channelService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MerchantService merchantService;


    @GetMapping("/balance")
    public Result balance(HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            return Result.error("商户管理员不存在！");
        }
        return Result.success(merchant.getBalance());
    }

    @GetMapping("/detail")
    public Result detail(HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            return Result.error("商户管理员不存在！");
        }
        HashMap<String, Object> retData = new HashMap<>();
        retData.put("merchant_id", merchant_id);
        retData.put("balance", merchant.getBalance());

        Member member = memberService.getMemberByUid(userSession.getUid());
        if(member == null) {
            return Result.error("商户管理员不存在！");
        }
        retData.put("login_ip", userSession.getAttribute(UserSession.K_LASTLOGINIP));
        retData.put("last_login_time", userSession.getAttribute(UserSession.K_LASTLOGINTIME));
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        // Get the beginning of the current day
        ZonedDateTime todayBeginTime = currentDateTime.toLocalDate().atStartOfDay(currentDateTime.getZone());
        // Get the end of the current day
        ZonedDateTime todayEndTime = todayBeginTime.plusDays(1).minusSeconds(1);
        // Convert to timestamps if needed (optional)
        long todayBeginTimestamp = todayBeginTime.toEpochSecond();
        long todayEndTimestamp = todayEndTime.toEpochSecond();
        List<Map> withdrawInfos = procedureService.selectByQuery(" count(if(state = 2, true, null)) as w_sc, ifnull(sum(if(state = 2, apply_amount, 0)), 0) as w_sa, count(if(state = 3, true, null)) as w_cc from wd_orders " +
                " where merchant_id=" + merchant_id + " and substr(created_at,1,10) >=" + todayBeginTimestamp + " and substr(created_at,1,10) <=" + todayEndTimestamp);
        if(withdrawInfos == null || withdrawInfos.size() == 0) {
            return Result.error("withdraw 查询失败!");
        }
        Map withdrawInfo = withdrawInfos.get(0);
        retData.put("withdraw_sc", withdrawInfo.get("w_sc"));
        retData.put("withdraw_sa", withdrawInfo.get("w_sa"));
        retData.put("withdraw_cc", withdrawInfo.get("w_cc"));

        List<Map> depositInfos = procedureService.selectByQuery(" count(if(state = 1, true, null)) as d_sc, ifnull(sum(if(state = 1, amount, 0)), 0) as d_sa, count(if(state = 0, true, null)) as d_cc from wd_deposit " +
                " where merchant_id=" + merchant_id + " and substr(created_at,1,10) >=" + todayBeginTimestamp + " and substr(created_at,1,10) <=" + todayEndTimestamp);
        if(depositInfos == null || depositInfos.size() == 0) {
            return Result.error("deposit 查询失败!");
        }
        Map depositInfo = withdrawInfos.get(0);
        retData.put("deposit_sc", depositInfo.get("d_sc"));
        retData.put("deposit_sa", depositInfo.get("d_sa"));
        retData.put("deposit_cc", depositInfo.get("d_cc"));
        return Result.success(retData);
    }

    @GetMapping("/chkselfcharge")
    public Result chkselfcharge(HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            return Result.error("商户管理员不存在！");
        }
        String channel_id = merchant.getChannel_id();
        Channel channel = channelService.getById(channel_id);
        if(channel == null) {
            return Result.error("该商户通道不存在!");
        }
//        boolean ret = (channel.getSelfCharge() == "true")
        return Result.success(true);
    }

    @PostMapping("/update/password")
    public Result updatePassword(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session){
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String token = httpRequest.getHeader("T");
        String old_password = decParams.get("old_password").toString();
        String new_password = decParams.get("new_password").toString();
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            return Result.error("商户管理员不存在！");
        }
        String uid = userSession.getUid();
        Member member = memberService.getMemberByUid(userSession.getUid());
        if(member == null) {
            return Result.error("商户管理员不存在！");
        }
        if(member.getState() == 0) {
            return Result.error("当前账号已禁用！");
        }
        String oldEncPassword = HashUtils.hashPassword(member.getUsername(), old_password);
        String newEncPassword = HashUtils.hashPassword(member.getUsername(), new_password);
        if(!oldEncPassword.equals(member.getPassword())) {
            return Result.error("原始密码错误！");
        }
        if(newEncPassword.equals(member.getPassword())) {
            return Result.success("密码修改成功！");
        }
        member.setPassword(newEncPassword);
        memberService.update(member);
        return Result.success("密码修改成功！");
    }

    @PostMapping("/view/key")
    public Result dokey(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session){
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String token = httpRequest.getHeader("T");
        String password = decParams.get("password").toString();
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        String uid = userSession.getUid();
        Member member = memberService.getMemberByUid(userSession.getUid());
        if(member == null) {
            return Result.error("该用户不存在！");
        }
        String encPassword = HashUtils.hashPassword(member.getUsername(), password);
        if(!member.getPassword().equals(encPassword)) {
            return Result.error("原始密码错误！");
        }

        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            return Result.error("该商户不存在！");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        return Result.success(ppk);
    }

}
