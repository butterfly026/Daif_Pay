package com.daifubackend.api.controller.agent;

import com.daifubackend.api.pojo.*;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.AgentService;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.MerchantWhitelistService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import com.daifubackend.api.utils.JwtUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/agent/member")
public class AgentsController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantWhitelistService merchantWhitelistService;


    @PostMapping(value="/login")
    public Result merchantLogin(@RequestBody String requestBody, HttpServletRequest request, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        if(!decParams.containsKey("username")) {
            return Result.error("用户名或密码错误");
        }
        if(!decParams.containsKey("password")) {
            return Result.error("密码不能为空");
        }
        if(!decParams.containsKey("code")) {
            return Result.error("验证码不能为空");
        }
//        if ($this->mem->get($username) == "lock") {
//            $this->responser->loginResponse(false, "登录太快了，休息一下!");
//            exit;
//        }
//        $this->mem->set($username, "lock", 5);

        String username = (String)decParams.get("username");
        String password = (String)decParams.get("password");
        String encPassword = HashUtils.hashPassword(username, password);
        if(username.isEmpty()) {
            return Result.error("用户名或密码错误1");
        } else {
            Agent userinfo = agentService.getAgentByUsername(username);
            if(userinfo == null) {
                return Result.error("用户名或密码错误2");
            }
            if(userinfo.getState() == 0) {
                return Result.error("账户已关闭");
            }
            if(!userinfo.getPassword().equals(encPassword)) {
                return Result.error("用户名或密码错误");
            }
            int code = Integer.parseInt(decParams.get("code").toString());
            if(!EncDecUtils.verifyGoogle2fa(userinfo.getGoogle(), code)) {
                return Result.error("验证码错误");
            }

            String uid = userinfo.getUid();
            username = userinfo.getUsername();
            userinfo.setUpdated_at((int)(System.currentTimeMillis() / 1000));
            userinfo.setUpdated_ip(CommonUtils.getIpAddress(request));
            agentService.update(userinfo);

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", userinfo.getUid());
            claims.put("username", userinfo.getUsername());
            String jwt = JwtUtils.generateJwt(claims);  //jwt包涵当前登录员工信息

            UserSession userSession = new UserSession();
            userSession.setAttribute(UserSession.K_TOKEN, jwt);
            userSession.setAttribute(UserSession.K_UID, userinfo.getUid());
            userSession.setAttribute(UserSession.K_USERNAME, userinfo.getUsername());
            userSession.setAttribute(UserSession.K_CREATETIME, (int)(System.currentTimeMillis() / 1000));
            userSession.setAttribute(UserSession.K_GOOGLEKEY, userinfo.getGoogle());
            userSession.setAttribute(UserSession.K_CURRENTIP, CommonUtils.getIpAddress(request));
            userSession.setAttribute(UserSession.K_USERAGENT, true);
            userSession.setAttribute(UserSession.K_LASTLOGINIP, userinfo.getUpdated_ip());
            userSession.setAttribute(UserSession.K_LASTLOGINTIME, userinfo.getUpdated_at());
            session.setAttribute(jwt, userSession);

            return Result.success(jwt);
        }
    }

    @GetMapping("/logout")
    public Result merchantLogout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                        @RequestParam(defaultValue = "10") Integer page_size,
                        String username, Short state, String merchant_id) {
        //调用service分页查询
        PageBean pageBean = agentService.page(page,page_size,username,state, merchant_id);
        return Result.success(pageBean);
    }


    @GetMapping("/nonce")
    public Result nonce() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return Result.success(gAuth.createCredentials().getKey());
    }

    @GetMapping("/info/detail")
    public Result detail(HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession agentSession = (UserSession)session.getAttribute(token);


        Agent member = agentService.getAgentByUid(agentSession.getUid());
        if(member == null) {
            return Result.error("商户管理员不存在！");
        }
        HashMap<String, Object> retData = new HashMap<>();
        retData.put("username", agentSession.getUName());
        retData.put("login_ip", agentSession.getAttribute(UserSession.K_LASTLOGINIP));
        retData.put("last_login_time", agentSession.getAttribute(UserSession.K_LASTLOGINTIME));
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        // Get the beginning of the current day
        ZonedDateTime todayBeginTime = currentDateTime.toLocalDate().atStartOfDay(currentDateTime.getZone());
        // Get the end of the current day
        ZonedDateTime todayEndTime = todayBeginTime.plusDays(1).minusSeconds(1);
        // Convert to timestamps if needed (optional)
        long todayBeginTimestamp = todayBeginTime.toEpochSecond();
        long todayEndTimestamp = todayEndTime.toEpochSecond();

        List<Merchant> lstMerchant = merchantService.getByAgentId(agentSession.getUid());
        String merchant_ids = "";
        for (Merchant m : lstMerchant) {
            merchant_ids += merchant_ids.isEmpty() ? m.getId() : ("," + m.getId());
        }
        if(!merchant_ids.isEmpty()) {
            log.info(" sum(t_fee * agent_ratio / 100) today_earn from \n" +
                    " ( select sum(fee) as t_fee, m.agent_ratio from wd_orders o \n" +
                    " left join wd_merchant m on o.merchant_id = m.id " +
                    " where o.merchant_id in (" + merchant_ids + ") and substr(o.created_at,1,10) >=" + todayBeginTimestamp + " and substr(o.created_at,1,10) <=" + todayEndTimestamp + " group by o.merchant_id) todayTbl");
            List<Map> today_earn = procedureService.selectByQuery(" sum(t_fee * agent_ratio / 100) today_earn from \n" +
                    " ( select sum(fee) as t_fee, m.agent_ratio from wd_orders o \n" +
                    " left join wd_merchant m on o.merchant_id = m.id " +
                    " where o.merchant_id in (" + merchant_ids + ") and substr(o.created_at,1,10) >=" + todayBeginTimestamp + " and substr(o.created_at,1,10) <=" + todayEndTimestamp + " group by o.merchant_id) todayTbl");
            if(today_earn == null || today_earn.size() == 0) {
                return Result.error("withdraw 查询失败!");
            }
            Map earnInfo = today_earn.get(0);
            if(earnInfo != null) {
                retData.put("today_earn", earnInfo.get("today_earn"));
            }
            log.info(" sum(t_fee * agent_ratio / 100) total_earn from \n" +
                    " ( select sum(fee) as t_fee, m.agent_ratio from wd_orders o \n" +
                    " left join wd_merchant m on o.merchant_id = m.id " + " where o.merchant_id in (" + merchant_ids + ") group by o.merchant_id) totalTbl");
            List<Map> total_earn = procedureService.selectByQuery(" sum(t_fee * agent_ratio / 100) total_earn from \n" +
                    " ( select sum(fee) as t_fee, m.agent_ratio from wd_orders o \n" +
                    " left join wd_merchant m on o.merchant_id = m.id " + " where o.merchant_id in (" + merchant_ids + ") group by o.merchant_id) totalTbl");
            if(total_earn == null || total_earn.size() == 0) {
                return Result.error("withdraw 查询失败!");
            }
            earnInfo = total_earn.get(0);
            if(earnInfo != null) {
                retData.put("total_earn", earnInfo.get("total_earn"));
            }
        } else {
            retData.put("today_earn", 0);
            retData.put("total_earn", 0);
        }



//        List<Map> depositInfos = procedureService.selectByQuery(" count(if(state = 1, true, null)) as d_sc, ifnull(sum(if(state = 1, amount, 0)), 0) as d_sa, count(if(state = 0, true, null)) as d_cc from wd_deposit " +
//                " where merchant_id in (" + merchant_ids + ") and substr(created_at,1,10) >=" + todayBeginTimestamp + " and substr(created_at,1,10) <=" + todayEndTimestamp);
//        if(depositInfos == null || depositInfos.size() == 0) {
//            return Result.error("deposit 查询失败!");
//        }
//        Map depositInfo = withdrawInfos.get(0);
//        retData.put("deposit_sc", depositInfo.get("d_sc"));
//        retData.put("deposit_sa", depositInfo.get("d_sa"));
//        retData.put("deposit_cc", depositInfo.get("d_cc"));
        return Result.success(retData);
    }

    @PostMapping("/update/password")
    public Result updatePassword(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
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
        Agent agent = agentService.getAgentByUid(userSession.getUid());
        if(agent == null) {
            return Result.error("商户管理员不存在！");
        }
        if(agent.getState() == 0) {
            return Result.error("当前账号已禁用！");
        }
        String oldEncPassword = HashUtils.hashPassword(agent.getUsername(), old_password);
        String newEncPassword = HashUtils.hashPassword(agent.getUsername(), new_password);
        if(!oldEncPassword.equals(agent.getPassword())) {
            return Result.error("原始密码错误！");
        }
        if(newEncPassword.equals(agent.getPassword())) {
            return Result.success("密码修改成功！");
        }
        agent.setPassword(newEncPassword);
        agentService.update(agent);
        return Result.success("密码修改成功！");
    }

}
