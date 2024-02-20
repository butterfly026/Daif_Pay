package com.daifubackend.api.controller.merchant;

import com.daifubackend.api.pojo.*;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.MerchantWhitelistService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import com.daifubackend.api.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/merchant/member")
public class MerchantMemberController {

    @Autowired
    private MemberService memberService;

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
            Member userinfo = memberService.getMemberByUsername(username);
            if(userinfo == null) {
                return Result.error("用户名或密码错误2");
            }
            String merchant_id = userinfo.getMerchant_id();
            short is_merchant_admin = userinfo.getIsadmin();
            List<MerchantWhitelist> merchantList = merchantWhitelistService.getByMerchantId(merchant_id);
            if(merchantList == null || merchantList.size() == 0) {
                return Result.error("商户没有设置白名单，登录失败!");
            }
            boolean isWhiteIp=false;
            for(MerchantWhitelist merchantWhite : merchantList) {
                String ip = merchantWhite.getIp();
                if(ip != null && ip.equals(CommonUtils.getIpAddress(request))) {
                    isWhiteIp = true;
                    break;
                } else if(ip == null || ip.equals("0.0.0.0")) {
                    isWhiteIp = false;
                    break;
                }
            }
            if(!isWhiteIp) {
                log.error("IP地址不合法，登录失败!  账号：[$username] IP：" + CommonUtils.getIpAddress(request));
                return Result.error("IP地址 " + CommonUtils.getIpAddress(request) + " 不合法，登录失败! ");
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
            memberService.update(userinfo);

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
            userSession.setAttribute(UserSession.K_USERMERCHANT, true);
            userSession.setAttribute(UserSession.K_MERCHANTID, userinfo.getMerchant_id());
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
        PageBean pageBean = memberService.page(page,page_size,username,state, merchant_id);
        return Result.success(pageBean);
    }

    @PostMapping(path="/insert", consumes = "application/x-www-form-urlencoded")
    public Result insert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Member member = mapper.convertValue(decParams, Member.class);
        String code = decParams.get("code").toString();
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        long sid = userSession.getSID();
        String merchant_id = userSession.getMerchant_id();
        String merc_gc = userSession.getGoogleKey();
        if(code == null || code.isEmpty()) {
            return Result.error("验证码不能为空");
        } else {
            if(!EncDecUtils.verifyGoogle2fa(merc_gc, Integer.parseInt(code))) {
                return Result.error("验证码错误");
            }
        }
        Member newMember = new Member();
        newMember.setUid(CommonUtils.makeID(18));
        newMember.setUsername(member.getUsername().toLowerCase());
        newMember.setPassword(HashUtils.hashPassword(member.getUsername(), member.getPassword()));
        newMember.setCreated_by_uid(userSession.getAttribute(UserSession.K_UID).toString());
        newMember.setCreated_by_name(userSession.getAttribute(UserSession.K_USERNAME).toString());
        newMember.setIsadmin((short)1);
        newMember.setMerchant_id(merchant_id);
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

    @PostMapping("/update")
    public Result update(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String password = decParams.get("password").toString();
        String google = decParams.get("google").toString();
        String uid = decParams.get("uid").toString();
        Short state = CommonUtils.parseInt(decParams.get("state").toString()).shortValue();
        int code = CommonUtils.parseInt(decParams.get("code").toString());
        Float agent_ratio = CommonUtils.parseFloat(decParams.get("agent_ratio").toString());
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        Member updateMember = new Member();
        updateMember.setState(state);
        updateMember.setUid(uid);
        Member member = memberService.getMemberByUid(uid);
        String gkey = userSession.getGoogleKey();
        if(gkey == null || gkey.isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }
        if(member == null) {
            log.error("更新信息失败！ 商户:[" + member.getUid() + "] 不存在!" );
            return Result.error("更新信息失败！");
        }
        if(!EncDecUtils.verifyGoogle2fa(gkey, code)) {
            return Result.error("验证码错误");
        }
        if(password != null && !password.isEmpty()) {
            updateMember.setPassword(HashUtils.hashPassword(member.getUsername(), password));
        }
        if(google != null && !google.isEmpty()) {
            updateMember.setGoogle(google);
        }
        if(agent_ratio != null)

        memberService.update(updateMember);
        log.info("更新商户管理信息 操作员:[{} - {}] 信息：[uid:{} - un:{}]",
                userSession.getAttribute(UserSession.K_USERNAME).toString(),
                CommonUtils.getIpAddress(httpRequest), member.getUid(), member.getUsername());
        return Result.success("增加成功！");
    }

    @GetMapping("/delete")
    public Result delete(@RequestParam String uid, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        if(userSession == null) {
            return Result.error("不能删除自身账号!");
        }
        log.info("删除商户管理 操作员：[{} - {}] 信息：[uid:{}]", userSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        memberService.delete(uid);
        log.info("删除商户管理 成功 操作员：[{} - {}] 信息：[uid:{}]", userSession.getUName(), CommonUtils.getIpAddress(httpRequest),uid);
        return Result.success("删除成功！");
    }

    @GetMapping("/nonce")
    public Result nonce() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return Result.success(gAuth.createCredentials().getKey());
    }

    @GetMapping("/agent/nonce")
    public Result agentNodke() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return Result.success(gAuth.createCredentials().getKey());
    }
}
