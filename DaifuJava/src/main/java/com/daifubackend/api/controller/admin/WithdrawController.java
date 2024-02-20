package com.daifubackend.api.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.controller.BaseCacheController;
import com.daifubackend.api.pojo.Order;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.OrderService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/withdraw")
public class WithdrawController extends BaseCacheController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChannelService channelService;
    @Autowired
    private ProcedureService procedureService;


    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @PostMapping("/list")
    public Result list(@RequestBody String requestBody) {
        log.info("Withdraw list");
        HashMap<String, Object> newParams = (HashMap<String, Object>) EncDecUtils.decodePostParam(requestBody);
        Set<String> methodNamesSet = newParams.keySet();
        int page = 1;
        int page_size = 10;
        for(String str: methodNamesSet) {
            if(str.equals("page_size")){
                page_size = CommonUtils.parseInt(newParams.get(str).toString());
            } else if(str.equals("page")) {
                page = CommonUtils.parseInt(newParams.get(str).toString());
            } else if(str.equals("st")) {
                newParams.put("st", CommonUtils.strtotime(Objects.requireNonNull(newParams.get("st")).toString()));
            } else if(str.equals("et")) {
                newParams.put("et", CommonUtils.strtotime(Objects.requireNonNull(newParams.get("st")).toString()));
            } else {
                newParams.put(str, newParams.get(str));
            }
        }
        PageBean pageBean = orderService.page(page,page_size, newParams);
        return Result.success(pageBean);
    }

    @PostMapping("/reject")
    public Result reject(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String merchant_id = decParams.get("merchant_id").toString();
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        String orderId = id;
        String username = adminSession.getUName();
        String uid = adminSession.getUid();
        Order order = orderService.getDataById(orderId);

        if(order == null) {
            log.error("手动拒绝 <提现订单错误> 订单:[{}]", orderId);
            return Result.error("提现订单[" + orderId + "]错误");
        }
        if(isCallbackLocked(orderId) && CommonUtils.preg_match("#" + GlobalConsts.FIRST_MANUAL_FLAG + "#", order.getErr())) {
            log.error("手动拒绝 <订单还未收到服务器通知> 订单：[{}] 商户:[{} - {}] 操作员：[{}]",
                    orderId, merchant_id, order.getMerchant_name(), adminSession.getUName());
            rejectUnlock(orderId);
            return Result.error("提现订单[" + orderId + "]错误");
        }

        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("手动拒绝 <商户不存在>[{}]", merchant_id);
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }

        if(merchant.getState() == OrderState.MANUAL_REVIEW) {
            log.error("手动拒绝 <订单还未收到服务器通知> 订单：[{}] 商户:[{} - {}] 金额：[{}]",
                    orderId, merchant_id, order.getMerchant_name(), order.getApply_amount());
            rejectUnlock(orderId);
            return Result.error("提现已处理");
        }

        if(isRejectLocked(orderId)) {
            log.error("手动拒绝 <重复提交> 订单：[{}] 商户:[{} - {}]",
                    orderId, merchant_id, order.getMerchant_name());
            return Result.error("手动拒绝 <安全锁定中稍后再试> 订单 [" + orderId + "] ！");
        }

        log.info("手动拒绝 <回滚订单> 订单：[{}] 商户:[{} - {}] 金额：[{}] 余额：[{}] 操作员：[{}]",
                orderId, merchant_id, order.getMerchant_name(), order.getApply_amount(), merchant.getBalance(), adminSession.getUName());

        Map res = CommonUtils.creditFee(merchant_id, orderId, order.getApply_amount(), order.getFee(),
                GlobalConsts.PAYMENT_FAILD_BACK, GlobalConsts.PAYMENT_FEE_BACK, "+", "手动拒绝 <回滚订单>", procedureService);
        if(!(boolean)res.get("ret")) {
            log.info("手动拒绝 <回滚订单失败>:[{}] 商户:[{} - {}] 金额：[{}] 余额：[{}]",
                    orderId, merchant_id, order.getMerchant_name(), order.getApply_amount(), merchant.getBalance());
            rejectUnlock(orderId);
            return Result.error("操作失败 信息：" + res.get("msg"));
        }
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setReview_at((int)(System.currentTimeMillis() / 1000));
        updateOrder.setReview_by_uid(adminSession.getUid());
        updateOrder.setReview_by_name(adminSession.getUName());
        updateOrder.setState(OrderState.MANUAL_FAILED);
        orderService.update(updateOrder);
        rejectUnlock(orderId);
        return Result.success("操作成功！");
    }

    @PostMapping("/query")
    public Result query(@RequestBody String requestBody,
                         HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String channel_id = decParams.get("channel_id").toString();
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        String orderId = id;
        Order order = orderService.getDataById(orderId);
        if(order == null) {
            return Result.error("找不到订单 [" + orderId + "]");
        }
        Merchant merchant = merchantService.getById(order.getMerchant_id());
        if(merchant == null) {
            return Result.error("商户ID [" + order.getMerchant_id() + "]");
        }

        Channel channel = channelService.getById(channel_id);
        if(channel == null) {
            return Result.error("上游ID [" + channel_id + "] 错误");
        }
        String ppk = CommonUtils.decryptPPK(channel.getPpk(), CommonUtils.ppkCryptKey);
        channel.setPpk(ppk);
        HashMap<String, Object> params = new HashMap<>();

        params.put("out_trade_no", orderId);
        params.put("mch_id", channel.getAccount());
        params.put("ppk", channel.getPpk());
        params.put("gateway", channel.getGateway());
        params.put("channel_id", channel.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        params.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {}));
        params.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {}));
        params.put("orderInfo", objectMapper.convertValue(order, new TypeReference<Map<String, Object>>() {}) );

        Map<String, Object> ret = CommonUtils.requireChannelAndCall(channel.getShortname(), "order", params);
        int retCode = (int) ret.get("ret");
        String result = (String)ret.get("result");
        log.info("手动查询 - {} 订单:[{}] 商户[{} - {}] 返回: [{}] msg: [{}]",
                channel.getShortname(), orderId, merchant.getId(), merchant.getName(), retCode, result);
        int orderState = CommonUtils.channelState2orderState(retCode, order.getApply_amount() > merchant.getConfirm());
        String osname = OrderState.label(order.getState());
        String csname = OrderState.label(orderState);
        Result res = Result.success("当前订单状态: [" + osname + "]  查询状态：[" + csname + "]");
        if(order.getState() < OrderState.MANUAL_REVIEW) {
            log.info("手动查询 订单状态已经是: [" + csname + "] 无需查询！ 信息：[{}]", result);
            SetCallbackLock(orderId, 0, false);
            return res;
        }
        if(orderState == OrderState.UNKNOW) {
            log.info("手动查询 <订单出现未知状态> 订单:[{}]  状态:[{}] 商户[{} - {}] 通道：[{} - {}] 信息：[{}]",
                    orderId, retCode, merchant.getId(), merchant.getName(), channel.getName(), channel.getShortname(),  result);
            return res;
        }
        if(orderState == OrderState.AUTO_FAILED || orderState != OrderState.SUCCESS) {
            SetCallbackLock(orderId, 0, false);
        }
        if(isMerchantModeB(merchant.getId()) && orderState != OrderState.SUCCESS) {
            log.info("手动查询 <商户模式B> 订单:[{}]  商户[{} - {}] 通道：[{} - {}] 信息：[{}]",
                    orderId, merchant.getId(), merchant.getName(),channel.getName(), channel.getShortname(),  result );
            orderService.orderChange("", "手动查询 (商户模式B) 订单还未成功:[" + result + "] 通道：[" + channel.getName() + " - " + channel.getShortname() + "]", OrderState.MANUAL_REVIEW, orderId);
            return res;
        }
        log.info("手动查询 变更订单状态- 订单:[" + orderId + "] 商户[" + merchant.getId() + " - " + merchant.getName() + "] code:[" + orderState + "] - " + OrderState.label(orderState));
        if(orderState == OrderState.AUTO_FAILED || orderState == OrderState.MANUAL_FAILED) {
            log.info("手动查询 订单失败 回滚- 订单:[{}]  商户[{} - {}]", orderId, merchant.getId(), merchant.getName());
            Map feeResult = CommonUtils.creditFee(merchant.getId(), orderId, order.getApply_amount(),
                    order.getFee(), GlobalConsts.PAYMENT_FAILD_BACK, GlobalConsts.PAYMENT_FEE_BACK, "+",
                    "手动查询 <回滚订单>", procedureService);
            boolean retVal = (boolean) feeResult.get("ret");
            if(!retVal) {
                log.info("手动查询 回滚订单失败 订单:[{}]  商户[{} - {}]", orderId, merchant.getId(), merchant.getName());
                return res;
            }
        }

        orderService.orderChange("", result, orderState, orderId);
        return res;
    }

    @PostMapping("/approve")
    Result approve(@RequestBody String requestBody,
                   HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String channel_id = decParams.get("channel_id").toString();

        String token = httpRequest.getHeader("T");
        String orderId = id;
        String lockkey = orderId;
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Order order = orderService.getDataById(orderId);
        if(order == null) {
            log.error("手动出款 <提现ID错误> 订单：[{}]" , orderId);
            return Result.error("提现ID错误");
        }
        Merchant merchant = merchantService.getById(order.getChannel_id());
        String notify_url = order.getCallback_url();
        String order_channelId = order.getChannel_id();
        if(merchant == null) {
            log.error("手动出款 <订单关联的商户不存在> 订单：[{}] 商户:[{}]", orderId, order.getMerchant_id());
            return Result.error("订单关联的商户 商户 [" + order.getMerchant_id() + "] 不存在!");
        }

        Channel channel = channelService.getById(channel_id);
        if(channel == null) {
            log.error("手动出款 <订单关联的通道不存在> 通道：[{}]  订单：[{}] 商户:[{} - {}] 操作员：[{}]",
                    channel_id, orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
            return Result.error("订单关联的通道 [" + channel_id + "] 不存在");
        }

        if(channel.getState() == ChannelState.DISABLED) {
            log.error("手动出款 <通道已禁用> 通道：[{} - {} - {}]  订单：[{}] 商户:[{} - {}] 操作员：[{}]" ,
                    channel.getName(), channel.getShortname(), channel.getId(), orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
            return Result.error("手动出款 通道 [" + channel.getName() + " - " + channel.getShortname() + "] 已禁用!");
        }
        if(order_channelId != channel_id) {
            log.error("手动出款 <支付通道已切换> 通道：" + order_channelId + "-" + channel_id);
            order_channelId = channel_id;
            order.setChannel_id(channel_id);
        }
        if(order.getState() < OrderState.MANUAL_REVIEW) {
            log.error("手动出款 <提现已处理> 订单：[{}] 商户:[{} - {}] 操作员：[{}]",
                    orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
            return Result.error("提现已处理");
        }
        if(!isMerchantModeB(order.getMerchant_id())) {
            if(IsLockedProcedure(lockkey)) {
                String errMsg = String.format("手动出款 <重复提交> 订单：[%s] 商户:[%n - %s] 操作员：[%s]",
                        orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
                log.error(errMsg);
                return Result.error(errMsg);
            }
            LockProcedure(lockkey, 60*3);
        }
        String ppk = CommonUtils.decryptPPK(channel.getPpk(), CommonUtils.ppkCryptKey);
        channel.setPpk(ppk);

        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("channel_id", channel.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        params.put("merchantInfo", objectMapper.convertValue(merchant, new TypeReference<Map<String, Object>>() {}));
        params.put("channelInfo", objectMapper.convertValue(channel, new TypeReference<Map<String, Object>>() {}));
        params.put("orderInfo", objectMapper.convertValue(order, new TypeReference<Map<String, Object>>() {}) );
        log.info("手动出款 <执行通道> [{} - create] 订单:[{}] 商户[{} - {}]  操作员：[{}]",
                channel.getShortname(), orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
        Map ret = CommonUtils.requireChannelAndCall(channel.getShortname(), "create", params);
        try{
            JSONObject vj = (JSONObject) JSON.parse(ret.get("result").toString());
            int retCode = (int) ret.get("ret");
            String result = (String)ret.get("result");
            log.info("手动出款 <脚本create 执行返回> 订单[{}] 商户:[{} - {}] 操作员：[{}] code: {},msg: {}",
                    orderId, merchant.getId(), merchant.getName(), adminSession.getUName(), retCode, result);
            int odt = CommonUtils.channelState2orderState(retCode, true);
            int notifyOrderState = CommonUtils.orderState2notifyOrderState(odt);
            int poll = vj.getIntValue("poll");
            if(odt == -1) {
                log.error("手动出款 <订单出现未知状态> 订单:[{}] 状态:[{}] 商户[{} - {}] 通道：[{} - {}] 信息：[{}]",
                        orderId, retCode,merchant.getId(), merchant.getName(), channel.getName(), channel.getShortname(),result );
                orderService.orderChange("", "手动出款 订单出现未知状态 请及时联系开发人员 信息：[" + result + "]", OrderState.MANUAL_REVIEW, orderId);
                return Result.error("手动出款 <订单出现未知状态> 状态:[" + retCode + "]");
            }
            SetCallbackLock(orderId, CommonUtils.CALL_BACK_LOCK_TIME, true);
            if(retCode == ChannelOrderState.SUCCESS && poll == 1) {
                orderService.orderChange(order_channelId, result, order.getState() != OrderState.DISPENSING ? OrderState.DISPENSING : -1, orderId);
                HashMap<String, Object> queueParams = new HashMap<>();
                queueParams.put("orderid", orderId);
                queueParams.put("shortname", channel.getShortname());
                queueParams.put("key", CommonUtils.INNER_ORDER_KEY);
                CommonUtils.PutQueryQueue(queueParams);
                log.info("手动出款 <提交成功> 订单 [{}] 商户[{} - {}]  操作员：[{}] 需要后台查询稍等...",
                        orderId, merchant.getId(), merchant.getName(), adminSession.getUName());
                return Result.success("手动出款 提交成功 订单 [" + orderId + "] 需要后台查询稍等...");
            }
            result = CommonUtils.unicode2Chinese(result);
            if(retCode == ChannelOrderState.UNKNOW_ERROR) {
                UnlockProcedure(lockkey);
            }
            log.info("手动出款 <检查模式B> 订单:[{}] 商户：[{} - {}] 模式:[" + merchant.getRemarks() + "]-[" + isMerchantModeB(merchant.getId()) + "] orderState:{}",
                    orderId, merchant.getId(), merchant.getName(), odt);
            if(isMerchantModeB(merchant.getId()) && odt != OrderState.SUCCESS) {
                log.info("手动出款 <商户模式B> 订单:[$orderId] 商户：[$merchant_id - $merchant_name] 通道：[$channelName - $shortname] 信息:[$result]");
                orderService.orderChange(order_channelId, "手动出款 (商户模式B)等待通道返回:[" + result + "]", OrderState.MANUAL_REVIEW, orderId);
                SetCallbackLock(orderId, 0, false);
                return Result.success("手动出款 <商户模式B> 等待上游返回 通道：[" + channel.getName()  + " - " + channel.getShortname()+ "]");
            }
            Result funcRet = Result.success();
            if(odt == OrderState.AUTO_FAILED || odt == OrderState.MANUAL_FAILED) {
                Map res = CommonUtils.creditFee(merchant.getId(), orderId, order.getApply_amount(),
                        order.getFee(), GlobalConsts.PAYMENT_FAILD_BACK, GlobalConsts.PAYMENT_FEE_BACK, "+","手动出款 <失败 回滚订单>",procedureService);
                boolean retval = (boolean)res.get("ret");
                String msg = (String)res.get("msg").toString();
                if(!retval) {
                    log.error("手动出款 <帐变回滚失败>  订单：[$orderId] 商户:[$merchant_id - $merchant_name]");
                    Order upOrder = new Order();
                    upOrder.setId(orderId);
                    upOrder.setChannel_id(order_channelId);
                    orderService.update(upOrder);
                    return Result.error("手动出款 <帐变回滚失败> 信息：" + msg);
                }
                log.info("手动出款 <失败> [$orderId] 商户[$merchant_id - $merchant_name] 操作员：[$username] 金额:[$apply_amount]");
                funcRet = Result.error("手动出款 失败");
            } else if(odt == OrderState.DISPENSING) {
                log.info("手动出款 <出款中> [$orderId] 商户:[$merchant_id - $merchant_name] 金额:[$apply_amount] 操作员：[$username]");
                funcRet = Result.success("出款中...");
            } else if(odt == OrderState.SUCCESS) {
                UnlockProcedure(lockkey);
                log.info("手动出款 <成功> [$orderId] 商户:[$merchant_id - $merchant_name] 金额:[$apply_amount] 操作员：[$username]");
                funcRet = Result.success("手动出款 成功");
            }
            Order upOrder = new Order();
            upOrder.setId(orderId);
            upOrder.setErr(result);
            if(odt != order.getState()) {
                order.setState(odt);
                upOrder.setState(odt);
            }
            upOrder.setChannel_id(order_channelId);

            log.info("手动出款 订单:[$orderId] 商户:[$merchant_id - $merchant_name] <写入状态>:[$orderState - " + OrderState.label(order.getState()) + "]");
            orderService.update(upOrder);
            UnlockProcedure(lockkey);
            return funcRet;
        }catch (Exception e) {
            return Result.error("ERROR");
        }
    }
    @PostMapping("/notify")
    Result notify(@RequestBody String requestBody,
                   HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String token = httpRequest.getHeader("T");
        String orderId = id;
        String lockkey = orderId;
        UserSession adminSession = (UserSession) session.getAttribute(token);
        Order order = orderService.getDataById(orderId);
        if (order == null) {
            log.error("手动通知 <订单号错误> [{}]", orderId);
            return Result.error("订单号错误[" + orderId + "]");
        }

        if (isCallbackLocked(orderId)) {
            log.error("手动通知 <订单还未收到服务器通知> 订单：[$orderid] 商户:[$merchant_id - $merchant_name] 操作员：[$uname]" );
            return Result.error("手动通知 <订单还未收到服务器通知，请稍后再试！>");
        }

        Merchant merchant = merchantService.getById(order.getMerchant_id());
        String notify_url = order.getCallback_url();
        String order_channelId = order.getChannel_id();
        if (merchant == null) {
            log.error("手动通知 <订单关联的商户不存在>  订单：[{}] 商户:[{}]", orderId, order.getMerchant_id());
            return Result.error("订单关联的商户 [" + order.getMerchant_id() + "] 不存在!");
        }
        String merchant_mode= merchant.getRemarks();
        String merchant_name = merchant.getName();
        if(order.getState() == OrderState.MANUAL_REVIEW || order.getState() == OrderState.DISPENSING) {
            log.error("手动通知 <提现中的单子,不能手工回调> 订单：[$orderid] 商户：[$merchant_id - $merchant_name] 操作员：[$uname]");
            return Result.error("提现中的单子,不能手工回调");
        }
        if(order.getCallback_url() == null || order.getCallback_url().length() < 5) {
            log.error("手动通知 <回调地址错误> 订单：[$orderid] 商户：[$merchant_id - $merchant_name] 操作员：[$uname] 地址：[$notify_url]");
            return Result.error("回调地址[" + order.getCallback_url() + "]错误");
        }

        log.info("手动通知 <通知商户> 订单：[$orderid] 商户：[$merchant_id - $merchant_name] 操作员：[$uname] 地址：[$notify_url]");
        int notifyOrderState = CommonUtils.orderState2notifyOrderState(order.getState());
        if(notifyOrderState == NotifyOrderState.SUCCESS || notifyOrderState == NotifyOrderState.FAILED || notifyOrderState == NotifyOrderState.REJECT) {
            CommonUtils.notifyMerchant(order.getCallback_url(), order.getMerchant_serial(), orderId, merchant.getPpk(), 3, notifyOrderState);
            return Result.success("手动通知 <通知成功>");
        }
        return Result.success("订单状态未变更无需通知!");
    }

    @PostMapping("/success")
    Result success(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String channel_id = decParams.get("channel_id").toString();
        String token = httpRequest.getHeader("T");
        String orderId = id;
        String lockkey = orderId;
        UserSession adminSession = (UserSession) session.getAttribute(token);
        Order order = orderService.getDataById(orderId);
        if (order == null) {
            log.error("手动成功 <订单号错误>[{}]", orderId);
            UnlockProcedure(lockkey);
            return Result.error("手动成功 订单 [" + orderId + "] 错误");
        }

        if (order.getState() < OrderState.MANUAL_REVIEW) {
            log.error("手动成功 <提现已处理> 订单：[{}] 商户:[{} - {}] 操作员：[{}]",
                    orderId, order.getMerchant_id(), order.getMerchant_name(), adminSession.getUName());
            return Result.error("提现已处理");
        }

        if(isMerchantModeB(order.getMerchant_id())) {
            if(IsLockedProcedure(lockkey)) {
                return Result.error("手动成功 订单 [" + orderId + "] 已锁定 请勿重复提交!");
            }
            LockProcedure(lockkey);
        }
        Order upOrder = new Order();
        upOrder.setId(orderId);
        upOrder.setChannel_id(channel_id);
        upOrder.setReview_at((int)(System.currentTimeMillis() / 1000));
        upOrder.setApply_amount(order.getApply_amount());
        upOrder.setReview_by_name(adminSession.getUName());
        upOrder.setState(OrderState.SUCCESS);
        orderService.update(upOrder);
        UnlockProcedure(lockkey);
        return Result.success("手动成功 操作成功！");
    }

    @PostMapping("/menu")
    Result menu() {
        return Result.success(orderService.getMenu());
    }
}
