package com.daifubackend.api.controller.merchant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.controller.BaseCacheController;
import com.daifubackend.api.exception.ApiExceptionHandler;
import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.pojo.WhiteApi;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.*;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.consts.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.daifubackend.api.utils.CommonUtils.createRule;
import static com.daifubackend.api.utils.CommonUtils.notifyMerchant;
import static java.lang.Thread.sleep;

@Slf4j
@RestController
@ControllerAdvice(assignableTypes = ApiExceptionHandler.class)
@RequestMapping("/merchant/api")
public class ApiController extends BaseCacheController {
    @Autowired
    ChannelService channelService;

    @Autowired
    ApiWhitelistService apiWhitelistService;

    @Autowired
    OrderService orderService;

    @Autowired
    ProcedureService procedureService;

    @Autowired
    private DepositService depositService;

    @Autowired
    ConfigService configService;

    @PostMapping(value = "/create")
    public Result create(@RequestBody MultiValueMap reqBody, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();

        rules.put("account", createRule(10, 25, "账号错误", "string", "string"));
        rules.put("id", createRule(1, 32, "订单号无效", "string", "string"));
        rules.put("notify_url", createRule(4, 300, "通知地址错误", "string", "string"));
        rules.put("time", createRule(10, 13, "时间戳错误", "string", "string"));
        rules.put("bank_name", createRule(2, 120, "API银行名错误", "string", "string"));
        rules.put("bank_user", createRule(2, 50, "开户人错误", "string", "string"));
        rules.put("bank_card", createRule(4, 50, "卡号错误", "string", "string"));
        rules.put("bank_open", createRule(2, 50, "开户行错误", "string", "string"));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string"));

        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = reqBody.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, reqBody.getFirst(str));
        }
        Object[] ret = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) ret[0]) {
            log.error("API-create <参数错误> " + ret[1].toString());
            return Result.error1(ret[1].toString());
        }
        if (isDisabledAllPay()) {
            log.error("系统交易暂时中止(API)！");
            return Result.error1("系统交易暂时中止(API)！");
        }
        String merchant_id = newParams.get("account").toString();
        float apply_amount = CommonUtils.parseFloat(newParams.get("apply_amount").toString());
        String merchant_serial = newParams.get("id").toString();
        String notify_url = newParams.get("notify_url").toString();
        String bank_name = newParams.get("bank_name").toString();
        String bank_user = newParams.get("bank_user").toString();
        String bank_card = newParams.get("bank_card").toString();
        String bank_open = newParams.get("bank_open").toString();
        String sign = newParams.get("sign").toString();
        String withdrawQueryUrl = newParams.containsKey("withdrawQueryUrl") ? newParams.get("withdrawQueryUrl").toString() : "";
        String callToken = newParams.containsKey("callToken") ? newParams.get("callToken").toString() : "";
        newParams.remove("withdrawQueryUrl");
        newParams.remove("callToken");
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            UnlockProcedure(merchant_serial);
            return Result.error1("(1)商户 [" + merchant_id + "] 不存在!");
        }
        float balance = merchant.getBalance();
        String channel_id = merchant.getChannel_id();
        float withdraw_fee = merchant.getWithdraw_fee();
        float withdraw_scale = merchant.getWithdraw_scale();
        float api_withdraw = merchant.getApi_withdraw();
        String merchant_name = merchant.getName();
        short state = merchant.getState();
        int need_reverse_check = merchant.getNeed_reverse_check();
        if (need_reverse_check == 1) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> reverse_check_param = new HashMap<>();
            reverse_check_param.put("merchantId", merchant_id);
            reverse_check_param.put("money", apply_amount);
            reverse_check_param.put("orderNo", merchant_serial);
            reverse_check_param.put("token", callToken);
            reverse_check_param.put("target", bank_card);
            reverse_check_param.put("ownerName", bank_user);
            String reverse_check_resp = CommonUtils.httpPostRequest(withdrawQueryUrl, reverse_check_param);
            log.info(reverse_check_resp);
            JSONObject reverseObj = JSON.parseObject(reverse_check_resp);
            if (reverseObj == null || reverseObj.getIntValue("code") == 500) {
                return Result.error1("订单serial [" + merchant_serial + "] 反查结果响应500");
            } else if (reverseObj.getIntValue("code") == 200) {
                if (reverseObj.getIntValue("status") == 1) {
                    log.info("反查通过:" + reverseObj.getString("msg"));
                } else {
                    log.info("反查结果失败:" + reverseObj.getString("msg"));
                    return Result.error1("订单serial [" + merchant_serial + "] 反查结果失败:" + reverseObj.getString("msg"));
                }
            } else {
                return Result.error1("订单serial [" + merchant_serial + "] 反查结果响应异常 [" + reverseObj.getIntValue("code") + "]");
            }
        }

        if (IsLockedProcedure(merchant_serial)) {
            log.error("createAsyncProc 重复提交 商户：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            return Result.error1("(0)订单serial [" + merchant_serial + "] 请勿重复提交!");
        }

        LockProcedure(merchant_serial, 60 * 10);
        if (apply_amount <= 0) {
            log.error("createAsyncProc 提现金额 小于0或不能为空 商户：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            return Result.error1("(0-1)提现金额 小于0或不能为空 不存在!");
        }

        float fee = CommonUtils.calc(withdraw_fee, apply_amount) + withdraw_scale;
        float total = apply_amount + fee;
        if (api_withdraw == 0) {
            log.error("createAsyncProc 提现金额 API 接口已禁用 商户：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            UnlockProcedure(merchant_serial);
            return Result.error1("(1-1)商户 [" + merchant_id + "] API 接口已禁用！");
        }

        if (state == 0) {
            log.error("createAsyncProc 商户已禁用：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            UnlockProcedure(merchant_serial);
            return Result.error1("(1-2)商户 [" + merchant_id + "] API 接口已禁用！");
        }

        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("createAsyncProc 商户验签失败！：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            UnlockProcedure(merchant_serial);
            return Result.error1("(2)商户 [" + merchant_id + "] 验签失败！");
        }

        Channel channel = channelService.getById(channel_id);
        if (channel == null) {
            log.error("createAsyncProc 商户通道不存在！：[{} - {}] 商户serial:[{}]",
                    merchant_id, merchant_name, merchant_serial);
            UnlockProcedure(merchant_serial);
            return Result.error1("(3)商户 [" + merchant_id + "] 验签失败！");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            UnlockProcedure(merchant_serial);
            return resCheck;
        }
        if (balance < total) {
            log.error("createAsyncProc 余额不足 商户：[{} - {}] 余额:[{} - {}]",
                    merchant_id, merchant_name, total, balance);
            UnlockProcedure(merchant_serial);
            return Result.error1("(4)商户 [" + merchant_id + "] 余额不足!");
        }
        state = (short) OrderState.DISPENSING;
        float min_limit = merchant.getMin_limit();
        float max_limit = merchant.getMax_limit();
        if (apply_amount < min_limit || apply_amount > max_limit) {
            log.error("createAsyncProc 不在出款范围内 商户：[{} - {}]  金额 [{} <{} - {}>] ",
                    merchant_id, merchant_name, apply_amount, min_limit, max_limit);
            UnlockProcedure(merchant_serial);
            return Result.error1("(5)商户 [" + merchant_id + "] 金额 [" + apply_amount + "] 不在出款范围内!");
        }
        Map<String, Boolean> banks = Banks.getBanks();
        if (banks.get(bank_name) == null) {
            log.error("createAsyncProc 银行不存在 商户：[" + merchant_id + "-" + merchant_name + "]  银行：[" + bank_name + "]");
            UnlockProcedure(merchant_serial);
            return Result.error1("(6)银行 [" + bank_name + "] 不存在");
        }
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("merchant_id", merchant_id);
        parameters.put("merchant_serial", merchant_serial);
        List<Order> oldOrders = orderService.list(parameters);
        if (oldOrders != null && oldOrders.size() > 0) {
            log.error("createAsyncProc 订单已经存在 商户：[{} - {}]  商户serial：[{}] ", merchant_id, merchant_name, merchant_serial);
            UnlockProcedure(merchant_serial);
            return Result.error1("(7)商户 " + merchant_id + " : 订单 [" + oldOrders.get(0).getId() + "] 已经存在");
        }
        int nRes = CommonUtils.createOrder(merchant_id, OrderFlag.API, apply_amount, fee, notify_url, merchant_serial, merchant_name,
                CommonUtils.getIpAddress(httpRequest), channel_id, bank_name, bank_user, bank_card, bank_open, state,
                GlobalConsts.API, GlobalConsts.PAY_FEE, "createAsyncProc", "", procedureService, this, true);
        UnlockProcedure(merchant_serial);
        if (nRes == 1) {
            return Result.success0("创建订单成功!");
        } else if (nRes == 2) {
            return Result.error1("创建订单失败!");
        } else {
            return Result.error1("创建订单失败  出现未知错误!");
        }
    }

    public Result checkPermission(Merchant merchant, Channel channel, String ipAddr) {
        if (merchant.getState() == MerchantState.DISABLED) {
            log.error("checkPermission 商户 [{}] 已关闭", merchant.getId());
            return Result.error1("check1 商户 [" + merchant.getId() + "] 已关闭！");
        }
        if (merchant.getApi_withdraw() == 0) {
            log.error("checkPermission 商户 [{}] API功能未开启", merchant.getId());
            return Result.error1("check2 商户 [" + merchant.getId() + "] API功能未开启！");
        }
        List<WhiteApi> whiteApis = apiWhitelistService.getByMerchantIp(merchant.getId(), "");
        if (whiteApis == null || whiteApis.size() == 0) {
            log.error("createAsyncProc 商户 [{} - {}] 未设置API白名单调用失败",
                    merchant.getId(), merchant.getName());
            return Result.error1("check3 商户 [" + merchant.getId() + "-" + merchant.getName() + "] 未设置API白名单调用失败");
        }
        boolean isApiWhiteIp = false;
        String cip = ipAddr;
        for (WhiteApi info : whiteApis) {
            String ip = info.getIp();
            if (ip.equals(ipAddr)) {
                isApiWhiteIp = true;
                break;
            }
            if (ip.equals("0.0.0.0")) {
                isApiWhiteIp = true;
                break;
            }
        }
        if (!isApiWhiteIp) {
            log.error("check5 商户 [{} - {}] IP:{} 不在 API 白名单中！",
                    merchant.getId(), merchant.getName(), cip);
            return Result.error1("check5 商户 [" + merchant.getId() + " - " + merchant.getName() + "] IP:" + cip + " 不在 API 白名单中！");
        }
        if (channel.getState() == ChannelState.DISABLED) {
            log.error("check5 商户 [{} - {}] 通道 [{}] 已关闭",
                    merchant.getId(), merchant.getName(), channel.getId());
            return Result.error1("check4 通道 [" + channel.getId() + "] 已关闭!");
        }
        return null;
    }

    @PostMapping(value = "/order")
    public Result order(@RequestBody MultiValueMap reqBody, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();

        rules.put("account", createRule(10, 25, "账号错误", "string", "string"));
        rules.put("id", createRule(1, 32, "订单号无效", "string", "string"));
        rules.put("time", createRule(10, 13, "时间戳错误", "string", "string"));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string"));

        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = reqBody.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, reqBody.getFirst(str));
        }
        Object[] ret = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) ret[0]) {
            log.error("API-create <参数错误> " + ret[1].toString());
            return Result.error1(ret[1].toString());
        }

        String merchant_id = newParams.get("account").toString();
        String merchant_serial = newParams.get("id").toString();
        String time = newParams.get("time").toString();
        String sign = newParams.get("sign").toString();
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            UnlockProcedure(merchant_serial);
            log.error("商户 [" + merchant_id + "] 不存在！ ");
            return Result.error1("(1)商户 [" + merchant_id + "] 不存在!");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("商户 [" + merchant_id + "] 验签失败！ ");
            return Result.error1("商户 [" + merchant_id + "] 验签失败！ ");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if (channel == null) {
            log.error("通道 [" + merchant.getChannel_id() + "] 不存在!");
            return Result.error1("通道 [" + merchant.getChannel_id() + "] 不存在!");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            return resCheck;
        }
        newParams.put("orderid", merchant_serial);
        newParams.put("sign", sign);
        newParams.put("merchantInfo", merchant);
        newParams.put("channelInfo", channel);
        return orderProc(newParams, 0);
    }

    private Result orderProc(HashMap<String, Object> params, int innercall) {
        if (params.containsKey("orderid")) {
            String orderid = params.get("orderid").toString();
            String lockey = "orderProc" + orderid;
            if (IsLockedProcedure(lockey)) {
                return Result.error1("主动查询订单 一定时间内请勿重复查询");
            }
            LockProcedure(lockey, 10);

            Order order = orderService.getDataById(orderid);
            if (order == null) {
                log.error("主动查询订单 订单 [" + orderid + "] 不存在");
                return Result.error1("主动查询订单 订单 [" + orderid + "] 不存在");
            }
            String merchant_id = order.getMerchant_id();
            String channel_id = order.getChannel_id();
            int stateOI = order.getState();
            float apply_amount = order.getApply_amount();
            String merchant_serial = order.getMerchant_serial();
            String notify_url = order.getCallback_url();

            Merchant merchant = merchantService.getById(merchant_id);
            if (merchant == null) {
                log.error("商户:[" + merchant_id + "] 不存在!");
                return Result.error1("商户 [" + merchant_id + "] 不存在!");
            }

            String merchant_name = merchant.getName();
            float balance = merchant.getBalance();
            float withdraw_fee = merchant.getWithdraw_fee();
            float withdraw_scale = merchant.getWithdraw_scale();
            int confirm = merchant.getConfirm();
            float fee = CommonUtils.calc(withdraw_fee, apply_amount) + withdraw_scale;

            if (stateOI == OrderState.SUCCESS || stateOI == OrderState.AUTO_FAILED || stateOI == OrderState.MANUAL_FAILED) {
                String logInfo = "主动查询订单 <订单状态已经明确> 订单：[" + orderid + "] 已经: " + OrderState.label(stateOI) + "商户：[" + merchant_id + " - " + merchant_name +
                        "] 余额：[" + balance + "]";
                log.error(logInfo);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(orderid);
                return Result.error1(logInfo);
            }

            short state = merchant.getState();
            short api_withdraw = merchant.getApi_withdraw();
            if (api_withdraw == 0 && innercall == 0) {
                String logInfo = "主动查询订单 <API 接口已禁用> 订单:[" + orderid + "] 商户：[" + merchant_id + " - " + merchant_name + "]";
                log.error(logInfo);
                return Result.error1(logInfo);
            }
            if (state == 0) {
                String logInfo = "主动查询订单 <商户已禁用> 订单:[" + orderid + "] 商户：[" + merchant_id + " - " + merchant_name + "]";
                log.error(logInfo);
                return Result.error1(logInfo);
            }

            Channel channel = channelService.getById(channel_id);
            if (channel == null) {
                String logInfo = "主动查询订单 <订单关联的通道> 订单:[" + orderid + "] 商户：[" + merchant_id + " - " + merchant_name + "] 通道：[" + channel_id + "]";
                log.error(logInfo);
                return Result.error1(logInfo);
            }

            String shortname = channel.getShortname();
            String channelName = channel.getName();
            String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
            channel.setPpk(ppk);
            ObjectMapper objectMapper = new ObjectMapper();
            params.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {
            }));
            params.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
            }));
            Map<String, Object> orderMapper = objectMapper.convertValue(order, new TypeReference<Map<String, Object>>() {
            });
            params.put("orderInfo", orderMapper);

            Map<String, Object> ret = CommonUtils.requireChannelAndCall(shortname, "order", params);
            int retCode = (int) ret.get("ret");
            String result = (String) ret.get("result");
            log.info("主动查询订单 <执行order> 订单:[{}] 商户：[{} - {}] {} - order 返回: {} msg: {}",
                    orderid, merchant_id, merchant_name, shortname, retCode, result);

            int orderState = CommonUtils.channelState2orderState(retCode, false);
            int notifyOrderState = CommonUtils.orderState2notifyOrderState(orderState);
            if (orderState == OrderState.UNKNOW) {
                log.error("主动查询订单 <订单出现未知状态> 订单:[{}] 状态:[{}] 商户[{} - {}] 通道：[{} - {}] 信息：[{}]",
                        orderid, retCode, merchant_id, merchant_name, channelName, shortname, result);
                orderService.orderChange("", "主动查询订单 订单出现未知状态 请及时联系开发人员 信息：[" + result + "]", OrderState.MANUAL_REVIEW, orderid);
                return Result.error1("主动查询订单 订单出现未知状态 请及时联系开发人员 信息：[" + result + "]"); //added by red
            }

            log.info("主动查询订单 <检查模式B> 订单:[{}] 商户：[{} - {}] 模式:[{}]-[{}] orderState:{}",
                    orderid, merchant_id, merchant_name, getMerchantMode(merchant_id), isMerchantModeB(merchant_id), orderState);
            if (isMerchantModeB(merchant_id) && orderState != OrderState.SUCCESS) {
                log.error("主动查询订单 <商户模式B>  订单:[{}] 状态:[{}] 商户[{} - {}] 通道：[{} - {}] 信息：[{}]",
                        orderid, retCode, merchant_id, merchant_name, channelName, shortname, result);
                orderService.orderChange("", "主动查询订单 (商户模式B) 订单还未成功：[" + result + "] 通道：[" + channelName + " - " + shortname + "]", OrderState.MANUAL_REVIEW, orderid);
                UnlockProcedure(merchant_serial);
                UnlockProcedure(orderid);
                return Result.error1("主动查询订单 (商户模式B) 订单还未成功：[" + result + "] 通道：[" + channelName + " - " + shortname + "]"); //added by red
            }
            Result res1 = Result.success0();
            if (orderState == OrderState.AUTO_FAILED || orderState == OrderState.MANUAL_FAILED) {
                log.info("主动查询订单 <回滚订单>:[{}] 商户：[{} - {}] 余额：[{}]",
                        orderid, merchant_id, merchant_name, balance);
                Map res = CommonUtils.creditFee(merchant_id, orderid, apply_amount, fee,
                        GlobalConsts.PAYMENT_FAILD_BACK, GlobalConsts.PAYMENT_FEE_BACK, "+", "主动查询订单 <回滚>", procedureService);
                if (!((boolean) res.get("ret"))) {
                    log.error("主动查询订单 <帐变回滚失败> 订单:[{}] 商户：[{} - {}] 通道：[{} - {}] 退出查询流程！",
                            orderid, merchant_id, merchant_name, channelName, shortname);
                    SetCallbackLock(orderid, 0, false);
                    UnlockProcedure(merchant_serial);
                    UnlockProcedure(orderid);
                    return Result.error1("主动查询订单 <帐变回滚失败>");
                }

                int ost = apply_amount > confirm ? OrderState.MANUAL_FAILED : OrderState.AUTO_FAILED;
                log.error("主动查询订单 <查询订单失败> 更新订单状态:[{}({})] 商户：[{} - {}]",
                        OrderState.label(orderState), orderState, merchant_id, merchant_name);
                orderService.orderChange("", "主动查询订单 <订单失败>", ost, orderid);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(merchant_serial);
                UnlockProcedure(orderid);
                res1 = Result.error1("主动查询订单 <查询订单失败>");
            } else if (orderState == OrderState.SUCCESS) {
                Order upOrder = new Order();
                upOrder.setId(orderid);
                upOrder.setState(OrderState.SUCCESS);
                log.info("主动查询订单 <更新订单>[{}] 状态:[{}({})] 商户：[{} - {}]",
                        orderid, OrderState.label(orderState), orderState, merchant_id, merchant_name);
                orderService.update(upOrder);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(merchant_serial);
                UnlockProcedure(orderid);
                res1 = Result.success0("订单成功 [" + orderid + "]");
            } else {
                res1 = Result.error1("出款中 [" + orderid + "]");
            }
            if (notifyOrderState == NotifyOrderState.SUCCESS || notifyOrderState == NotifyOrderState.FAILED || notifyOrderState == NotifyOrderState.REJECT) {
                CommonUtils.notifyMerchant(notify_url, merchant_serial, orderid, merchant.getPpk(), 3, notifyOrderState);
            }
            return res1;
        }
        Merchant merchant = (Merchant) params.get("merchantInfo");
        short api_withdraw = merchant.getApi_withdraw();
        String merchant_id = merchant.getId();
        String merchant_name = merchant.getName();
        float balance = merchant.getBalance();
        short state = merchant.getState();
        if (api_withdraw == 0 && innercall == 0) {
            log.error("order API 接口已禁用 商户：[{} - {}]", merchant_id, merchant_name);
            return Result.error1("(order)商户 [" + merchant_id + " - " + merchant_name + "] API 接口已禁用2！");
        }
        if (state == 0) {
            log.error("order 已禁用！ 商户：[{} - {}]", merchant_id, merchant_name);
            return Result.error1("(order)商户 [" + merchant_id + " - " + merchant_name + "] 已禁用！");
        }
        String merchant_serial = params.get("id").toString();
        Channel channel = (Channel) params.get("channelInfo");
        HashMap<String, Object> orderParams = new HashMap<>();
        orderParams.put("merchant_id", merchant_id);
        orderParams.put("merchant_serial", merchant_serial);
        List<Order> orders = orderService.list(orderParams);
        if (orders == null || orders.size() == 0) {
            log.error("order 不存在 商户：[{} - {}] 订单serial: [{}]", merchant_id, merchant_name, merchant_serial);
            return Result.error1("(order)商户 [" + merchant_id + " - " + merchant_name + "] 订单: [" + merchant_serial + "] 不存在！");
        }
        Order order = orders.get(0);
        String shortname = channel.getShortname();

        ObjectMapper objectMapper = new ObjectMapper();
        params.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {
        }));
        params.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
        }));
        Map<String, Object> orderMapper = objectMapper.convertValue(order, new TypeReference<Map<String, Object>>() {
        });
        params.put("orderInfo", orderMapper);
        Map<String, Object> ret = CommonUtils.requireChannelAndCall(shortname, "order", params);
        int retCode = (int) ret.get("ret");
        String result = (String) ret.get("result");
        log.info("SuperStar查询订单 <执行order> 订单:[" + merchant_serial + "] 商户：[" + merchant_id + " - " + merchant_name + "] " + shortname
                + " - order 返回: " + retCode + " msg: " + result);
        orderMapper.put("msg", result);
        return Result.success0(orderMapper);
    }

    @PostMapping(value = "/balance")
    public Result balance(@RequestBody MultiValueMap reqBody, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();

        rules.put("account", createRule(10, 25, "账号错误", "string", "string"));
        rules.put("time", createRule(10, 13, "时间戳错误", "string", "string"));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string"));

        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = reqBody.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, reqBody.getFirst(str));
        }
        Object[] ret = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) ret[0]) {
            log.error("API-create <参数错误> " + ret[1].toString());
            return Result.error1(ret[1].toString());
        }

        String merchant_id = newParams.get("account").toString();
        String time = newParams.get("time").toString();
        String sign = newParams.get("sign").toString();
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            log.error("商户 [" + merchant_id + "] 不存在!");
            return Result.error1("商户 [" + merchant_id + "] 不存在!");
        }
        String channel_id = merchant.getChannel_id();
        float balance = merchant.getBalance();
        short api_withdraw = merchant.getApi_withdraw();
        int state = merchant.getState();
        if (api_withdraw == 0) {
            log.error("(balance)商户 [" + merchant_id + "] API 接口已禁用！");
            return Result.error1("(balance)商户 [" + merchant_id + "] API 接口已禁用！");
        }
        if (state == 0) {
            log.error("(balance)商户 [" + merchant_id + "] 已禁用！");
            return Result.error1("(balance)商户 [" + merchant_id + "] 已禁用！");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("商户 [" + merchant_id + "] 验签失败！ ");
            return Result.error1("商户 [" + merchant_id + "] 验签失败！ ");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if (channel == null) {
            log.error("通道 [" + merchant.getChannel_id() + "] 不存在!");
            return Result.error1("通道 [" + merchant.getChannel_id() + "] 不存在!");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            return resCheck;
        }
        return Result.success0(balance);
    }

    @PostMapping(value = "/cb")
    public Result cb(MultiValueMap reqBody, HttpServletRequest httpRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("[Api-cb] 接收回调: postParams=" + mapper.writeValueAsString(reqBody));
            HashMap<String, Object> newParams = new HashMap<>();
            Set<String> methodNamesSet = reqBody.keySet();
            for (String str : methodNamesSet) {
                newParams.put(str, reqBody.getFirst(str));
            }

            String shortname = newParams.get("shortname").toString();
            log.info("Api-cb <进入回调流程> [{}] 回调方IP:[{}]", shortname, CommonUtils.getIpAddress(httpRequest));
            HashMap<String, Object> hMap = new HashMap<>();
            hMap.put("shortname", shortname);
            List<Channel> channels = channelService.getChannelByCondition(hMap);
            if (channels == null || channels.size() == 0) {
                hMap.clear();
                channels = channelService.getChannelByCondition(hMap);
                log.info("[Api-cb] <通道短名称无效> [{}] postParams={}", shortname, mapper.writeValueAsString(reqBody));
                String cinfo = "";
                for (Channel ch : channels) {
                    cinfo += "[" + ch.getName() + "] - [" + ch.getShortname() + "]\r\n";
                }
                log.info("Api-cb <通道数据>： \\r\\n{}", cinfo);
                return Result.error1("通道短名称无效");
            }
            Channel channel = channels.get(0);
            String channelName = channel.getName();
            String ppk = channel.getPpk();
            ppk = CommonUtils.encryptPPK(ppk, CommonUtils.ppkCryptKey);
            channel.setPpk(ppk);
            newParams.put("postParams", newParams);
            newParams.put("channelInfo", mapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
            }));
            Map<String, Object> cbRes = CommonUtils.requireChannelAndCall(shortname, "callback", newParams);
            int retCode = (int) cbRes.get("ret");
            String orderid = (String) cbRes.get("orderid");
            String stateInfo = (String) cbRes.get("stateInfo");
            if (retCode == -200 || orderid == "") {
                log.error("Api-cb <渠道不存在或订单为空> 渠道: [{}] 订单：[{}]", shortname, orderid);
                return Result.error1("渠道不存在或订单为空");
            }
            String lockey = "cb" + orderid;
            if (IsLockedProcedure(lockey)) {
                log.error("Api-cb <订单回调太快> 订单：[{}]", orderid);
            }
            LockProcedure(lockey, 50);
            Order order = orderService.getDataById(orderid);
            if (order == null) {
                log.error("Api-cb <订单不存在> 订单：[{}] ", orderid);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(lockey);
                UnlockProcedure(orderid);
                return null;
            }
            String merchant_id = order.getMerchant_id();
            int state = order.getState();
            float apply_amount = order.getApply_amount();
            String merchant_serial = order.getMerchant_serial();
            String notify_url = order.getCallback_url();
            String merchant_name = order.getMerchant_name();

            int orderstate = CommonUtils.channelState2orderState(retCode, false);
            if (orderstate == OrderState.SUCCESS || orderstate == OrderState.AUTO_FAILED || orderstate == OrderState.MANUAL_FAILED) {
                log.error("Api-cb 订单状态已经是: [{} - {}] 回调无效，退出回调流程！callback 执行返回：[{}] 订单：[{}] 商户:[{} - {}]",
                        state, OrderState.label(state), orderstate, orderid, merchant_id, merchant_name);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(lockey);
                UnlockProcedure(orderid);
                return null;
            }

            Merchant merchant = merchantService.getById(merchant_id);
            if (merchant == null) {
                log.error("Api-cb 订单关联的商户 [{} - {}] 不存在! 订单：[{}]",
                        merchant_id, merchant_name, orderid);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(lockey);
                return null;
            }

            float withdraw_fee = merchant.getWithdraw_fee();
            float withdraw_scale = merchant.getWithdraw_scale();
            float fee = CommonUtils.calc(withdraw_fee, apply_amount) + withdraw_scale;
            merchant_name = merchant.getName();

            int notifOrderState = CommonUtils.orderState2notifyOrderState(orderstate);
            if (orderstate == OrderState.UNKNOW) {
                log.error("Api-cb <订单出现未知状态> 订单:[{}] 状态:[{}] 商户[{} - {}] 通道：[{} - {}] ",
                        orderid, retCode, merchant_id, merchant_name, channelName, shortname);
                orderService.orderChange("", "Api-cb 订单出现未知状态 请及时联系开发人员", OrderState.MANUAL_REVIEW, orderid);
                return null;
            }

            log.info("Api-cb <检查模式B> 订单:[{}] 商户：[{} - {}] 模式:[{}]-[{}] orderState:{}",
                    orderid, merchant_id, merchant_name, getMerchantMode(merchant_id), isMerchantModeB(merchant_id), orderstate);
            if (isMerchantModeB(merchant_id) && orderstate != OrderState.SUCCESS) {
                log.info("Api-cb <检查模式B> 订单:[{}] 商户：[{} - {}] orderState:{}",
                        orderid, merchant_id, merchant_name, orderstate);
                orderService.orderChange("", "Api-cb (商户模式B) 发起失败:[" + stateInfo + "] 通道：[" + channelName + " - " + shortname + "] " + "等待人工审核", OrderState.MANUAL_REVIEW, orderid);
                SetCallbackLock(orderid, 0, false);
                UnlockProcedure(lockey);
                UnlockProcedure(orderid);
                return null;
            }

            if (orderstate == OrderState.MANUAL_FAILED || orderstate == OrderState.AUTO_FAILED) {
                SetCallbackLock(orderid, 0, false);
                Map res = CommonUtils.creditFee(merchant_id, orderid, apply_amount, fee, GlobalConsts.PAYMENT_FAILD_BACK, GlobalConsts.PAYMENT_FEE_BACK, "+", "API-cb <回滚订单>", procedureService);
                if (!(boolean) res.get("ret")) {
                    log.info("Api-cb 回滚订单失败 订单:[{}] 商户：[{} - {}] ",
                            orderid, merchant_id, merchant_name);
                    UnlockProcedure(lockey);
                    UnlockProcedure(orderid);
                    return null;
                }
            }
            Order upOrder = new Order();
            upOrder.setId(orderid);
            upOrder.setState(orderstate);
            upOrder.setErr(stateInfo);
            orderService.update(upOrder);
            notifyMerchant(notify_url, merchant_serial, orderid, merchant.getPpk(), 3, notifOrderState);
            SetCallbackLock(orderid, 0, false);
            UnlockProcedure(lockey);
            UnlockProcedure(orderid);
            return Result.success0();
        } catch (JsonProcessingException e) {
            return Result.error1("Invalid parameters");
        }
    }

    @PostMapping(value = "/pay")
    public Result pay_cash(@RequestParam MultiValueMap<String, Object> formData, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();
        rules.put("account", createRule(10, 25, "账号错误", "string", "string", true));
        rules.put("order_no", createRule(1, 300, "订单号无效(", "string", "string", true));
        rules.put("apply_amount", createRule(1, 9999999, "无效金额值", "number", "float", true));
        rules.put("notify_url", createRule(4, 300, "通知地址错误", "string", "string"));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string", true));
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("[Api-pay_cash] 接收回调: postParams=" + mapper.writeValueAsString(formData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.errorWithMsg1("接收回调");
        }
        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = formData.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, formData.getFirst(str));
        }
        String strReqData = JSON.toJSONString(newParams);
//        String strBody = CommonUtils.getBody(httpRequest);
//        HashMap<String, Object> newParams = new HashMap<>();
//        JSONObject jObj = JSON.parseObject(strBody);
//        Set<String> keySet = jObj.keySet();
//        for (String str : keySet) {
//            newParams.put(str, jObj.get(str));
//        }
        Object[] retCheck = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) retCheck[0]) {
            log.error("API-pay_cash <充值> " + retCheck[1].toString());
            return Result.errorWithMsg1(retCheck[1].toString());
        }

        String merchant_id = newParams.get("account").toString();
        String sign = newParams.get("sign").toString();
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            log.error("商户 [" + merchant_id + "] 不存在！ ");
            return Result.errorWithMsg1("(1)商户 [" + merchant_id + "] 不存在!");
        }
        short state = merchant.getState();

        if (state == 0) {
            log.error("充值 已禁用！ 商户：[{} - {}]", merchant_id, merchant.getName());
            return Result.errorWithMsg1("(充值)商户 [" + merchant_id + " - " + merchant.getName() + "] 已禁用！");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        log.info("ppk:::{}", ppk);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("商户 [" + merchant_id + "] 验签失败！ ");
            return Result.errorWithMsg1("商户 [" + merchant_id + "] 验签失败！ ");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if (channel == null) {
            log.error("通道 [" + merchant.getChannel_id() + "] 不存在!");
            return Result.errorWithMsg1("通道 [" + merchant.getChannel_id() + "] 不存在!");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            return Result.errorWithMsg1(resCheck.getData().toString());
        }
        float apply_amount = CommonUtils.parseFloat(newParams.get("apply_amount").toString());
        if (apply_amount <= 0) {
            log.error("createAsyncProc 充值金额 小于0或不能为空 商户：[{} - {}]",
                    merchant_id, merchant.getName());
            return Result.errorWithMsg1("(0-1)充值金额 小于0或不能为空 不存在!");
        }

        HashMap<String, Object> depositMap = new HashMap<>();
        depositMap.put("merchant_id", merchant_id);
        depositMap.put("channel_id", channel.getId());
        depositMap.put("order_no", newParams.get("order_no").toString());
        Deposit oldDeposit = depositService.getDataByCondition(depositMap);
        if (oldDeposit != null) {
            log.error("订单号存在 商户：[{} - {}] 订单号-{}",
                    merchant_id, merchant.getName(), newParams.get("order_no").toString());
            return Result.errorWithMsg1("订单号存在");
        }
        Deposit newDeposit = new Deposit();
        newDeposit.setId(CommonUtils.makeID(19));
        newDeposit.setAmount(apply_amount);
        newDeposit.setFlags(2);
        newDeposit.setMerchant_id(merchant_id);
        newDeposit.setCreated_ip(CommonUtils.getIpAddress(httpRequest));
        newDeposit.setReview_at(0);
        newDeposit.setReview_by_uid("0");
        newDeposit.setReview_by_name("[API 充值金额]-[" + channel.getName() + "]");
        newDeposit.setChannel_id(channel.getId());
        newDeposit.setOrder_no(newParams.get("order_no").toString());
        newDeposit.setCreated_by_name(merchant.getName());
        newDeposit.setCreated_by_uid(merchant.getId());
        newDeposit.setMerchant_remark("[API 充值金额]-[" + channel.getName() + "]");
        newDeposit.setReview_remark("");
        newDeposit.setState((short) 3);
        newDeposit.setApi_request_json(strReqData);
        newDeposit.setSent_notify_cnt(0);


        depositService.add(newDeposit);

        String shortname = channel.getShortname();

        ObjectMapper objectMapper = new ObjectMapper();
        newParams.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {
        }));
        newParams.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
        }));
        newParams.put("sign", sign);
        newParams.put("notify_domain_url", configService.getDataByKey("notify_domain_url") == null ? "http://18.166.247.242:3389/merchant/api/payment_notify" : configService.getDataByKey("notify_domain_url").getValue());
        newParams.put("trade_id", newDeposit.getId());


        Map<String, Object> ret = CommonUtils.requireChannelAndCall(shortname, "pay", newParams);
        newDeposit.setChannel_request_json(ret.get("request").toString());
        newDeposit.setChannel_response_json(ret.get("result").toString());
        int retCode = (int) ret.get("ret");
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("code", ret.get("code"));
        resMap.put("msg", ret.get("msg"));
        resMap.put("order_no", newParams.get("order_no").toString());
        if (retCode == 0) {
            depositService.update(newDeposit);
            resMap.put("trade_order_no", ret.get("trade_order_no"));
            resMap.put("cashier_url", ret.get("cashier_url"));
            return Result.success0(resMap);
        } else {
            newDeposit.setState((short) 0);
            depositService.update(newDeposit);
            return Result.error1(resMap);
        }
    }


    @PostMapping("/query_pay")
    public Result query_pay(@RequestParam MultiValueMap<String, Object> formData, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        rules.put("account", createRule(10, 25, "账号错误", "string", "string", true));
        rules.put("order_no", createRule(1, 300, "订单号无效(", "string", "string", true));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string", true));
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("[Api-query_pay] 接收回调: postParams=" + mapper.writeValueAsString(formData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.errorWithMsg1("接收回调");
        }
        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = formData.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, formData.getFirst(str));
        }
        Object[] retCheck = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) retCheck[0]) {
            log.error("API-query_pay <参数错误> " + retCheck[1].toString());
            return Result.errorWithMsg1(retCheck[1].toString());
        }

        String merchant_id = newParams.get("account").toString();
        String sign = newParams.get("sign").toString();
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            log.error("商户 [" + merchant_id + "] 不存在！ ");
            return Result.errorWithMsg1("(1)商户 [" + merchant_id + "] 不存在!");
        }
        short state = merchant.getState();

        if (state == 0) {
            log.error("充值 已禁用！ 商户：[{} - {}]", merchant_id, merchant.getName());
            return Result.errorWithMsg1("(充值)商户 [" + merchant_id + " - " + merchant.getName() + "] 已禁用！");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("商户 [" + merchant_id + "] 验签失败！ ");
            return Result.errorWithMsg1("商户 [" + merchant_id + "] 验签失败！ ");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if (channel == null) {
            log.error("通道 [" + merchant.getChannel_id() + "] 不存在!");
            return Result.errorWithMsg1("通道 [" + merchant.getChannel_id() + "] 不存在!");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            return Result.errorWithMsg1(resCheck.getData().toString());
        }
        newParams.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {
        }));
        newParams.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
        }));

        HashMap<String, Object> depositMap = new HashMap<>();
        depositMap.put("merchant_id", merchant_id);
        depositMap.put("channel_id", channel.getId());
        depositMap.put("order_no", newParams.get("order_no").toString());
        Deposit oldDeposit = depositService.getDataByCondition(depositMap);
        if (oldDeposit == null) {
            log.error("订单号存在 商户：[{} - {}] 订单号-{}",
                    merchant_id, merchant.getName(), newParams.get("order_no").toString());
            return Result.errorWithMsg1("订单号不在");
        }
        depositMap.clear();
        if(oldDeposit.getChannel_response_json() != null) {
            JSONObject jsonObject = JSON.parseObject(oldDeposit.getChannel_response_json());
            depositMap.put("msg", jsonObject.getString("msg"));
            if(oldDeposit.getState() == 1) {
                depositMap.put("trade_order_no", jsonObject.getString("outTradeNo"));
                depositMap.put("cashier_url", jsonObject.getString("cashierUrl"));
            }
            depositMap.put("code", oldDeposit.getState());
            return Result.success0(depositMap);
        } else {
            return Result.errorWithMsg1("充值未完成");
        }
    }

    @PostMapping(value = "/single_pay")
    public Result single_pay(@RequestParam MultiValueMap<String, Object> formData, HttpServletRequest httpRequest) {
        Map<String, Map<String, Object>> rules = new HashMap<>();
        rules.put("account", createRule(10, 25, "账号错误", "string", "string", true));
        rules.put("order_no", createRule(1, 300, "订单号无效", "string", "string", true));
        rules.put("bank_name", createRule(2, 50, "银行名称错误", "string", "string", true));
        //bank_code is ignorable
        rules.put("card_no", createRule(5, 19, "银行卡号错误", "string", "string", true));
        rules.put("holder_name", createRule(2, 32, "持卡人姓名错误", "string", "string", true));
        rules.put("bank_account_type", createRule(1, 2, "银行账户类型错误", "string", "string", true)); //'BU', 'BC'
        //bank_no is ignorable
        rules.put("apply_amount", createRule(1, 9999999, "无效金额值", "number", "float", true));
        rules.put("notify_url", createRule(4, 300, "通知地址错误", "string", "string"));
        rules.put("sign", createRule(4, 40, "签名格式错误", "string", "string", true));
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("[Api-single_pay] 接收回调: postParams=" + mapper.writeValueAsString(formData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.errorWithMsg1("接收回调");
        }
        HashMap<String, Object> newParams = new HashMap<>();
        Set<String> methodNamesSet = formData.keySet();
        for (String str : methodNamesSet) {
            newParams.put(str, formData.getFirst(str));
        }
        String strReqData = JSON.toJSONString(newParams);

        Object[] retCheck = CommonUtils.checkParam(rules, newParams);
        if (!(boolean) retCheck[0]) {
            log.error("API-single_pay <单笔代付> " + retCheck[1].toString());
            return Result.errorWithMsg1(retCheck[1].toString());
        }

        String merchant_id = newParams.get("account").toString();
        String sign = newParams.get("sign").toString();
        newParams.remove("sign");
        Merchant merchant = merchantService.getById(merchant_id);
        if (merchant == null) {
            log.error("API-single_pay 商户 [" + merchant_id + "] 不存在！ ");
            return Result.errorWithMsg1("(1)商户 [" + merchant_id + "] 不存在!");
        }
        short state = merchant.getState();

        if (state == 0) {
            log.error("API-single_pay 充值 已禁用！ 商户：[{} - {}]", merchant_id, merchant.getName());
            return Result.errorWithMsg1("(充值)商户 [" + merchant_id + " - " + merchant.getName() + "] 已禁用！");
        }
        String ppk = CommonUtils.decryptPPK(merchant.getPpk(), CommonUtils.ppkCryptKey);
        log.info("ppk:::{}", ppk);
        if (!CommonUtils.isVaileSign(newParams, ppk, sign)) {
            log.error("API-single_pay 商户 [" + merchant_id + "] 验签失败！ ");
            return Result.errorWithMsg1("商户 [" + merchant_id + "] 验签失败！ ");
        }
        Channel channel = channelService.getById(merchant.getChannel_id());
        if (channel == null) {
            log.error("API-single_pay 通道 [" + merchant.getChannel_id() + "] 不存在!");
            return Result.errorWithMsg1("通道 [" + merchant.getChannel_id() + "] 不存在!");
        }
        Result resCheck = checkPermission(merchant, channel, CommonUtils.getIpAddress(httpRequest));
        if (resCheck != null) {
            return Result.errorWithMsg1(resCheck.getData().toString());
        }
        float apply_amount = CommonUtils.parseFloat(newParams.get("apply_amount").toString());
        if (apply_amount <= 0) {
            log.error("Single_pay createAsyncProc 充值金额 小于0或不能为空 商户：[{} - {}]",
                    merchant_id, merchant.getName());
            return Result.errorWithMsg1("(0-1)充值金额 小于0或不能为空 不存在!");
        }

        String shortname = channel.getShortname();

        ObjectMapper objectMapper = new ObjectMapper();
        newParams.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {
        }));
        newParams.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {
        }));
        newParams.put("sign", sign);
        newParams.put("notify_domain_url", configService.getDataByKey("notify_domain_url") == null ? "http://18.166.247.242:3389/merchant/api/payment_notify" : configService.getDataByKey("notify_domain_url").getValue());
        newParams.put("trade_id", CommonUtils.genOrderId());


        Map<String, Object> ret = CommonUtils.requireChannelAndCall(shortname, "single_pay", newParams);
        int retCode = (int) ret.get("ret");
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("code", ret.get("code"));
        resMap.put("msg", ret.get("msg"));
        resMap.put("order_no", newParams.get("order_no").toString());
        if (retCode == 0) {
            resMap.put("trade_order_no", ret.get("trade_order_no"));
            resMap.put("cashier_url", ret.get("cashier_url"));
            return Result.success0(resMap);
        } else {
            return Result.error1(resMap);
        }
    }

    @PostMapping("/payment_notify")
    public Result payment_notify(HttpServletRequest httpRequest) {
        try {
            log.error("PAYMENT_NOTIFY>>>>>>>");
            httpRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success0("Success");
    }
}
