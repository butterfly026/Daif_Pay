package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.*;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.AgentService;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.OrderService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import com.daifubackend.api.utils.XXTEA;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProcedureService procedureService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                        @RequestParam(defaultValue = "10") Integer page_size,
                        String name, Short state, String channel_id) {
        log.info("分页查询,参数:{},{},{},{},{},{}",page,page_size,name,state);
        //调用service分页查询
        PageBean pageBean = merchantService.page(page,page_size,name,state, channel_id);
        return Result.success(pageBean);
    }

    @GetMapping("/delete")
    public Result delete(String id, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Merchant merchant = merchantService.getById(id);
        if(merchant == null) {
            log.error("删除商户 不存在商户：[$id] 操作员：[$uname]");
            return Result.error("商户 [" + id + "] 不存在!");
        }
        log.info("删除商户 商户：[{} - {}] 操作员：[{}]", id, merchant.getName(), adminSession.getUName());
        merchantService.deleteById(id);
        return Result.success("删除成功!");
    }

    @PostMapping("/adjust")
    public Result adjust(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String code = decParams.get("code").toString();
        String ct = decParams.get("ct").toString();
        String id = decParams.get("id").toString();
        Float amount = CommonUtils.parseFloat(decParams.get("amount").toString());
        String remark = decParams.get("remark").toString();
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("商户帐变 商户：[{}] 操作员：[{}] 金额：[{}]", id, adminSession.getUName(), amount);
        if(adminSession.getGoogleKey() == null || adminSession.getGoogleKey().isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }
        if(!EncDecUtils.verifyGoogle2fa(adminSession.getGoogleKey(), Integer.parseInt(code))) {
            return Result.error("验证码错误");
        }
        CashType cashType = orderService.getCashTypeById(ct);
        if(cashType == null) {
            log.error("商户帐变 帐变类型不存在 商户：[$merchant_id] 操作员：[$uname]");
            return Result.error("帐变类型不存在！");
        }
        String plus = "++";
        if(cashType.getOp().equals("-")) {
            plus = "--";
        }

        Map result = CommonUtils.creditFee(id, "merchant-adjust-" + (int)(System.currentTimeMillis() / 1000), amount,
                0f, CommonUtils.parseInt(ct), GlobalConsts.PAYMENT_FEE, plus, "上分 <调整商户>", procedureService);
        boolean ret = (boolean)result.get("ret");
        if(!ret) {
            return Result.error("上分 <调整失败>");
        }
        log.info("商户帐变 调整成功 商户：[$merchant_id] 操作员：[$uname] 金额：[$amount]");
        return Result.success("上分 调整成功!");
    }

    @PostMapping("/update")
    public Result update(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Merchant merchant = mapper.convertValue(decParams, Merchant.class);
        String code = decParams.get("code").toString();
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("商户帐变 商户：[$merchant_id] 操作员：[$uname] 金额：[$amount]");
        if(adminSession.getGoogleKey() == null || adminSession.getGoogleKey().isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }
        if(!EncDecUtils.verifyGoogle2fa(adminSession.getGoogleKey(), Integer.parseInt(code))){
            return Result.error("验证码错误");
        }

        if(merchant.getPpk() != null || merchant.getPpk().length() > 6) {
            String t = XXTEA.encryptToBase64String(merchant.getPpk(), CommonUtils.ppkCryptKey);
            merchant.setPpk(t);
        }
        log.info("更新商户信息 商户：[$id - $name] 操作员：[$uname] 变更：");
        merchantService.update(merchant);
        log.info("更新商户信息 成功 商户：[$id - $name] 操作员：[$uname]");
        return Result.success("更新商户信息成功！");
    }
    @PostMapping("/insert")
    public Result insert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Merchant merchant = mapper.convertValue(decParams, Merchant.class);
        String code = decParams.get("code").toString();
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("商户帐变 商户：[$merchant_id] 操作员：[$uname] 金额：[$amount]");
        if(adminSession.getGoogleKey() == null || adminSession.getGoogleKey().isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }

        if(!EncDecUtils.verifyGoogle2fa(adminSession.getGoogleKey(), Integer.parseInt(code))) {
            return Result.error("验证码错误");
        }


        if(merchant.getPpk() != null || merchant.getPpk().length() > 6) {
            String t = XXTEA.encryptToBase64String(merchant.getPpk(), CommonUtils.ppkCryptKey);
            merchant.setPpk(t);
        }
        merchant.setId(CommonUtils.makeID(19));
        merchant.setBalance(0f);
        merchant.setNeed_reverse_check(0);
        if(merchant.getRemarks() == null)
            merchant.setRemarks("");
        log.info("增加商户 [$id - $name] 操作员：[$uname] ");
        merchantService.save(merchant);
        log.info("增加商户 [$id - $name] 成功 操作员：[$uname] ");
        return Result.success(merchant.getId());
    }

    @GetMapping("/menu")
    public Result menu() {
        List<Merchant> mrs = merchantService.getMenus();
        for(Merchant mr : mrs) {
            if(mr.getState() == 0) {
                mr.setName(mr.getName() + "(关闭)");
            }
        }
        return Result.success(mrs);
    }

    @GetMapping("/member/delete")
    public Result deleteMember(@RequestParam String uid, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("删除商户管理 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        memberService.delete(uid);
        log.info("删除商户管理 成功 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        return Result.success("删除成功！");
    }

    @PostMapping(path="/member/insert", consumes = "application/x-www-form-urlencoded")
    public Result insertMember(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Member member = mapper.convertValue(decParams, Member.class);

        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Member newMember = new Member();
        newMember.setUid(CommonUtils.makeID(18));
        newMember.setUsername(member.getUsername().toLowerCase());
        newMember.setPassword(HashUtils.hashPassword(member.getUsername(), member.getPassword()));
        newMember.setCreated_by_uid(adminSession.getAttribute(UserSession.K_UID).toString());
        newMember.setCreated_by_name(adminSession.getAttribute(UserSession.K_USERNAME).toString());
        newMember.setIsadmin((short)1);
        newMember.setMerchant_id(member.getMerchant_id());
        newMember.setState(member.getState());
        newMember.setGoogle(member.getGoogle());
        newMember.setUpdated_ip(CommonUtils.getIpAddress(httpRequest));

        Merchant merchant = merchantService.getById(newMember.getMerchant_id());
        if(merchant == null) {
            log.error("添加商户管理 商户:[" + member.getMerchant_id() + "] 不存在!" );
            return Result.error("商户 [" + member.getMerchant_id() + "] 不存在!");
        }
        log.info("添加商户管理 商户:[{} - {}] 操作员：[{} - {}] 信息：[uid:{} - un:{}]",
                merchant.getId(),merchant.getName(), member.getCreated_by_name(), CommonUtils.getIpAddress(httpRequest), member.getUid(), member.getUsername());
        memberService.add(newMember);
        return Result.success("增加成功！");
    }

    @PostMapping("/member/update")
    public Result updateMember(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String password = decParams.get("password").toString();
        String google = decParams.get("google").toString();
        String uid = decParams.get("uid").toString();
        Short state = CommonUtils.parseInt(decParams.get("state").toString()).shortValue();

        UserSession adminSession = (UserSession)session.getAttribute(token);
        Member updateMember = new Member();
        updateMember.setState(state);
        updateMember.setUid(uid);
        Member member = memberService.getMemberByUid(uid);
        if(member == null) {
            log.error("更新信息失败！ 商户:[" + member.getUid() + "] 不存在!" );
            return Result.error("更新信息失败！");
        }
        if(password != null && !password.isEmpty()) {
            updateMember.setPassword(HashUtils.hashPassword(member.getUsername(), password));
        }
        if(google != null && !google.isEmpty()) {
            updateMember.setGoogle(google);
        }

        memberService.update(updateMember);
        log.info("更新商户管理信息 操作员:[{} - {}] 信息：[uid:{} - un:{}]",
                adminSession.getAttribute(UserSession.K_USERNAME).toString(),
                CommonUtils.getIpAddress(httpRequest), member.getUid(), member.getUsername());
        return Result.success("增加成功！");
    }

    @GetMapping("/member/list")
    public Result memberList(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       String name, Short state, String merchant_id) {
        //调用service分页查询
        PageBean pageBean = memberService.page(page,page_size,name,state, merchant_id);
        return Result.success(pageBean);
    }


    @GetMapping("/agent/delete")
    public Result deleteAgent(@RequestParam String uid, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("删除商户管理 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        agentService.delete(uid);
        log.info("删除商户管理 成功 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        return Result.success("删除成功！");
    }

    @PostMapping(path="/agent/insert", consumes = "application/x-www-form-urlencoded")
    public Result insertAgent(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Agent agent = mapper.convertValue(decParams, Agent.class);
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Agent newAgent = new Agent();
        newAgent.setUid(CommonUtils.makeID(18));
        newAgent.setUsername(agent.getUsername().toLowerCase());
        newAgent.setPassword(HashUtils.hashPassword(agent.getUsername(), agent.getPassword()));
        newAgent.setCreated_by_uid(adminSession.getAttribute(UserSession.K_UID).toString());
        newAgent.setCreated_by_name(adminSession.getAttribute(UserSession.K_USERNAME).toString());
        newAgent.setIsadmin((short)1);
        newAgent.setState(agent.getState() == null ? (short) 0 : (short) 1);
        newAgent.setGoogle(agent.getGoogle());
        newAgent.setUpdated_ip(CommonUtils.getIpAddress(httpRequest));

//        Merchant merchant = merchantService.getById(newAgent.getMerchant_id());
//        if(merchant == null) {
//            log.error("添加商户管理 商户:[" + agent.getMerchant_id() + "] 不存在!" );
//            return Result.error("商户 [" + agent.getMerchant_id() + "] 不存在!");
//        }
//        log.info("添加商户管理 商户:[{} - {}] 操作员：[{} - {}] 信息：[uid:{} - un:{}]",
//                merchant.getId(),merchant.getName(), agent.getCreated_by_name(), CommonUtils.getIpAddress(httpRequest), agent.getUid(), agent.getUsername());
        agentService.add(newAgent);
        return Result.success("增加成功！");
    }

    @PostMapping("/agent/update")
    public Result updateAgent(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String password = decParams.get("password").toString();
        String google = decParams.get("google").toString();
        String uid = decParams.get("uid").toString();
        Short state = CommonUtils.parseInt(decParams.get("state").toString()).shortValue();

        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Agent updateAgent = new Agent();
        updateAgent.setState(state);
        updateAgent.setUid(uid);
        Agent agent = agentService.getAgentByUid(uid);
        if(agent == null) {
            log.error("更新信息失败！ 商户:[" + agent.getUid() + "] 不存在!" );
            return Result.error("更新信息失败！");
        }
        if(password != null && !password.isEmpty()) {
            updateAgent.setPassword(HashUtils.hashPassword(agent.getUsername(), password));
        }
        if(google != null && !google.isEmpty()) {
            updateAgent.setGoogle(google);
        }

        agentService.update(updateAgent);
        log.info("更新商户管理信息 操作员:[{} - {}] 信息：[uid:{} - un:{}]",
                adminSession.getAttribute(UserSession.K_USERNAME).toString(),
                CommonUtils.getIpAddress(httpRequest), agent.getUid(), agent.getUsername());
        return Result.success("增加成功！");
    }


    @GetMapping("/agent/list")
    public Result agentList(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                             @RequestParam(defaultValue = "10") Integer page_size,
                             String name, Short state, String merchant_id) {
        //调用service分页查询
        PageBean pageBean = agentService.page(page,page_size,name,state, merchant_id);
        return Result.success(pageBean);
    }
    @GetMapping("/agent/menu")
    public Result agentMenu() {
        List<Agent> mrs = agentService.getMenus();
        for(Agent mr : mrs) {
            if(mr.getState() == 0) {
                mr.setUsername(mr.getUsername() + "(关闭)");
            }
        }
        return Result.success(mrs);
    }

}
