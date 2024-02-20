package com.daifubackend.api.controller;

import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.AdminWhitelist;
import com.daifubackend.api.service.admin.AdminService;
import com.daifubackend.api.service.admin.AdminWhitelistService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import com.daifubackend.api.utils.JwtUtils;
import com.daifubackend.api.utils.consts.GlobalConsts;
import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
public class LoginController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminWhitelistService adminWhitelistService;

    @Autowired
    private AntiSamy antiSamy;

    @PostMapping(value="/admin/member/login")
    public Result login(@RequestBody String requestBody, HttpServletRequest request, HttpSession session) {
        Admin admin = new Admin();

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

        String username = (String)decParams.get("username");
        String password = (String)decParams.get("password");
        String encPassword = HashUtils.hashPassword(username, password);
        Integer code = Integer.parseInt((String)decParams.get("code"));
        admin.setUsername(username);
        admin.setPassword(encPassword);

        String login_ip = CommonUtils.getIpAddress(request);
        log.info("用户登录:{} ip={}", admin, login_ip);
        Admin e = adminService.login(admin);
        //登录成功，生成令牌，下发令牌
        if (e != null) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", e.getUid());
            claims.put("username", e.getUsername());
            if(!EncDecUtils.verifyGoogle2fa(e.getGoogle(), code)) {
                return Result.error("验证码错误");
            }

            log.error(e.getUsername() + " is going to login ");
            if (e.getUsername().equals(GlobalConsts.ChannelAdminName)){
                String current_login_ip = CommonUtils.getIpAddress(request);
                AdminWhitelist adminWhitelistList = adminWhitelistService.getByIpAdmin(e.getUid(), current_login_ip);
                if (adminWhitelistList == null){
                    log.error(e.getUsername() + " 非法IP地址");
                    return  Result.error("非法IP地址");
                }
            }

            String jwt = JwtUtils.generateJwt(claims);

            UserSession adminSession = new UserSession();
            adminSession.setAttribute(UserSession.K_TOKEN, jwt);
            adminSession.setAttribute(UserSession.K_UID, e.getUid());
            adminSession.setAttribute(UserSession.K_USERNAME, e.getUsername());
            adminSession.setAttribute(UserSession.K_GOOGLEKEY, e.getGoogle());
            adminSession.setAttribute(UserSession.K_CURRENTIP, CommonUtils.getIpAddress(request));
            adminSession.setAttribute(UserSession.K_ISADMIN, true);
            adminSession.setAttribute(UserSession.K_CHANNELKEY, encPassword);
            session.setAttribute(jwt, adminSession);
            log.error(e.getUsername() + " " + session.getId() + " " + jwt + " " );
            return Result.success(jwt);
        }
        //登录失败，返回错误信息
        return Result.error("账号或密码错误");
    }

    @GetMapping("/admin/member/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }



}
