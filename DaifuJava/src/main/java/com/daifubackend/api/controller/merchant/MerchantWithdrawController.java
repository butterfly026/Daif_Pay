package com.daifubackend.api.controller.merchant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.controller.BaseCacheController;
import com.daifubackend.api.pojo.*;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.DepositService;
import com.daifubackend.api.service.OrderService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.Banks;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.daifubackend.api.utils.consts.OrderFlag;
import com.daifubackend.api.utils.consts.OrderState;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/merchant/withdraw")
public class MerchantWithdrawController extends BaseCacheController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       @RequestParam HashMap<String, Object> params) {
        PageBean pageBean = orderService.page(page,page_size, params);
        return Result.success(pageBean);
    }

    @GetMapping("/bank")
    public Result banks(HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("getBanks 该商户[" + merchant_id + "]不存在!");
            return Result.error("该商户不存在！");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if(channel == null) {
            return Result.error("该商户通道不存在！");
        }

//        return Result.success(CommonUtils.requireChannelAndCall("common", "banks", null));
        return Result.success(Banks.getBanks());
    }

    @PostMapping("/insert")
    public Result doInsert(@RequestBody String requestBody, HttpServletRequest request, HttpSession session) {
        //调用service分页查询
        Map reqBody = EncDecUtils.decodePostParam(requestBody);
        UserSession userSession = (UserSession)session.getAttribute(request.getHeader("T").toString());
        String merchant_id = userSession.getMerchant_id();
        float amount = CommonUtils.parseFloat(reqBody.get("amount").toString());
        String gcode = reqBody.get("code").toString();
        String gkey = userSession.getGoogleKey();
        String bank_name = reqBody.get("bank_name").toString();
        String bank_open = reqBody.get("bank_open").toString();
        String bank_user = reqBody.get("bank_user").toString();
        String bank_card = reqBody.get("bank_card").toString();
        String uname = userSession.getUName();
        if(isDisabledAllPay()) {
            log.error("系统交易暂时中止(单笔提现)！ 商户:[{}]", merchant_id);
            return Result.error("系统交易暂时中止(单笔提现)！");
        }
        if(IsLockedProcedure("" + merchant_id)) {
            log.error("手动提现 商户:[{}] 提交太快了!", merchant_id);
            return Result.error("WI 提交太快了!");
        }
        LockProcedure("" + merchant_id);
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("手动提现 商户:[{}] 不存在!", merchant_id);
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }

        if(merchant.getState() == 0) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("商户 [" + merchant_id + "] 已禁用！");
        }

        Channel channel = channelService.getById(merchant.getChannel_id());
        if(channel == null) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 商户没有上游通道！ 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("商户没有上游通道!");
        }

        if(gcode == null || gcode.isEmpty()) {
            UnlockProcedure("" + merchant_id);
            return Result.error("验证码不能为空");
        } else {
            if(!EncDecUtils.verifyGoogle2fa(gkey, Integer.parseInt(gcode))) {
                UnlockProcedure("" + merchant_id);
                return Result.error("验证码错误");
            }
        }

        if(merchant.getM_batch_withdraw() == 0) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 单笔提现已关闭，请联系管理员处理！ 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("单笔提现已关闭，请联系管理员处理");
        }
        if(channel.getState() == 0) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 通道已禁用！ 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("单笔提现已关闭，通道已禁用！");
        }
        float fee = CommonUtils.calc(merchant.getWithdraw_fee(), amount) + merchant.getWithdraw_scale();
        float total = amount + fee;
        if(amount < merchant.getMin_limit() || amount > merchant.getMax_limit()) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 <金额不在出款范围内> 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("金额" + amount + "不在出款范围内！[" + merchant.getMin_limit() + "-" + merchant.getMax_limit() + "]");
        }
        if(merchant.getBalance() < total) {
            UnlockProcedure("" + merchant_id);
            log.error("手动提现 余额+手续费 不足 商户:[{} - {} - {}] 已禁用！", merchant_id, uname, merchant.getName());
            return Result.error("余额+手续费 不足 金额：[" + total + "-" + merchant.getBalance() + "]");
        }
        String ip = userSession.getAttribute(UserSession.K_CURRENTIP).toString();
        log.info("手动提现 <发起> 商户:[{} - {} - {}] IP:[{}] 金额：[{}] 余额：[{}]",
                merchant_id, uname, merchant.getName(), ip, amount, merchant.getBalance());
        int state = OrderState.DISPENSING;
        String notify_url = "";
        String merchant_serial = CommonUtils.makeSerialID(32);
        int nRes = CommonUtils.createOrder(merchant_id, OrderFlag.MANUAL_SINGLE, amount, fee, notify_url, merchant_serial, merchant.getName(), ip, channel.getId(), bank_name,
                bank_user, bank_card, bank_open, state, GlobalConsts.MANULA, GlobalConsts.PAY_FEE, "手动提现", merchant.getPpk(), procedureService, this, true);
        if(nRes == 1) {
            return Result.success("创建订单成功!");
        }else if(nRes == 2) {
            return Result.error("创建订单失败!");
        }else {
            return Result.error("创建订单失败  出现未知错误!");
        }

    }

    @PostMapping("/batch")
    public Result batch(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map reqBody = EncDecUtils.decodePostParam(requestBody);
        String[] paramKey = new String[]{"金额",
                "收款卡号",
                "收款姓名",
                "收款银行",
                "开户银行"};
        int max_count = 100;

        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        String merchant_id = userSession.getMerchant_id();
        if(CommonUtils.parseLong(merchant_id) == 0) {
            return Result.error("商户管理员关联不存在！");
        }

        JSONArray data = JSON.parseArray(reqBody.get("data").toString());
        if(data == null || data.size() == 0) {
            return Result.error("错误 提交数据为空!");
        }
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("批量提现 <该商户不存在001> 商户[" + merchant_id + "]不存在!");
            return Result.error("批量提现 <该商户不存在001>");
        }

        if(isDisabledAllPay()) {
            log.error("批量提现 <系统交易暂时中止> 商户：[{} - {}] 操作员：[{} - {}]",
                    merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest));
            return Result.error("批量提现 <系统交易暂时中止>！");
        }
        if(merchant.getState() == 0) {
            log.error("批量提现 <商户已禁用> 商户：[{} - {}] 操作员：[{} - {}]",
                    merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest));
            return Result.error("商户 [" + merchant_id + "] 已禁用！");
        }

        Channel channel = channelService.getById(merchant.getChannel_id());
        if(channel == null) {
            log.error("批量提现 <商户上游通道不存在> 商户：[{} - {}] 操作员：[{} - {}] 通道：[{}]",
                    merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), merchant.getChannel_id());
            return Result.error("商户没有上游通道 [" + merchant.getChannel_id() + "]！");
        }

        if(merchant.getM_batch_withdraw() == 0) {
            log.error("批量提现 <批量提现已关闭> 商户：[{} - {}] 操作员：[{} - {}] 通道：[{}]",
                    merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), merchant.getChannel_id());
            log.error("批量提现 <批量提现已关闭> 商户：[$merchant_id - $merchant_name] 操作员：[$uname - $lgip] 通道：[$channel_id]");
            return Result.error("批量提现已关闭，请联系管理员处理!");
        }
        int count = 0;
        float total = 0;
        for(int i = 0; i < data.size(); i++ ){
            JSONObject item = data.getJSONObject(i);
            for(String vKey : paramKey) {
                if(!item.containsKey(vKey) || item.getString(vKey).isEmpty()) {
                    log.error("批量提现 <数据格式错误002> 商户：[{} - {}] 操作员：[{} - {}] 通道：[{}]",
                            merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), merchant.getChannel_id());
                    return Result.error("上传失败，错误的数据格式! 找不到：[" + vKey + "]");
                }
            }
            float amount = item.getFloat("amount");
            total += amount + CommonUtils.calc(merchant.getWithdraw_fee(), amount) + merchant.getWithdraw_scale();
            count++;
        }
        if(count > max_count) {
            return Result.error("批量提现 <单数超出 " + max_count + "单> 当前单数：" + count);
        }
        log.info("批量提现 <发起批量提现> 商户：[{} - {}] 操作员：[{} - {}] 总金额：[{}] 通道：[{}]",
                merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), total, merchant.getChannel_id());
        if(merchant.getBalance() < total) {
            log.error("批量提现 <发起批量提现> 商户：[{} - {}] 操作员：[{} - {}] 总金额：[{}-{}] 通道：[{}]",
                    merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), total, merchant.getBalance(), merchant.getChannel_id());
            return Result.error("余额+手续费 不足\\r\\n您总共需要: " + total);
        }
        for(int i = 0; i < data.size(); i++ ) {
            JSONObject item = data.getJSONObject(i);
            merchant = merchantService.getById(merchant_id);
            if(merchant == null) {
                log.error("批量提现 <该商户不存在002> 商户[" + merchant_id + "]不存在!");
                return Result.error("批量提现 <该商户不存在002>");
            }
            float amount = item.getFloat("金额");
            float fee = CommonUtils.calc(merchant.getWithdraw_fee(), amount) + merchant.getWithdraw_scale();
            total = amount + fee;
            String notify_url="";
            String bank_name= item.getString("收款银行");
            String bank_user=item.getString("收款姓名");
            String bank_card=item.getString("收款卡号");
            String bank_open= item.getString("开户银行").isEmpty() ? item.getString("收款银行") : item.getString("开户银行");

            int state= OrderState.DISPENSING;
            String merchant_serial = CommonUtils.makeSerialID(32);

            CommonUtils.createOrder(merchant_id, OrderFlag.MANUAL_BATCH, amount, fee, notify_url, merchant_serial, merchant.getName(),
                    CommonUtils.getIpAddress(httpRequest), merchant.getChannel_id(), bank_name, bank_user, bank_card, bank_open, state,
                    GlobalConsts.MANULA, GlobalConsts.PAY_FEE, "批量提现", merchant.getPpk(), procedureService, this, false);


        }
        log.info("批量提现 <结束批量提现-数据已提交> 商户：[{} - {}] 操作员：[{} - {}] 总金额：[{}-{}] 通道：[{}]",
                merchant_id, merchant.getName(), userSession.getUName(), CommonUtils.getIpAddress(httpRequest), total, merchant.getBalance(), merchant.getChannel_id());
        return Result.success("批量提现 <结束批量提现-数据已提交>");
    }
}
