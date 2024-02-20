package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Group;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.MemberService;
import com.daifubackend.api.service.admin.AdminService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.HashUtils;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/member")
public class MemberController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ChannelService channelService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                        @RequestParam(defaultValue = "10") Integer page_size,
                        String username, String group_id, Short state) {
        //调用service分页查询
        PageBean pageBean = adminService.page(page, page_size, username, group_id, state);
        return Result.success(pageBean);
    }

    @PostMapping(path="/insert", consumes = "application/x-www-form-urlencoded")
    public Result insert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Admin input_admin = mapper.convertValue(decParams, Admin.class);
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);

        Admin oldAdmin = adminService.getByUsername(input_admin.getUsername());
        if(oldAdmin != null) {
            log.error("添加管理员 该用户已存在 操作员：[{} - {}] ", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest));
            return Result.error("该用户已存在");
        }
        String ip = CommonUtils.getIpAddress(httpRequest);
        Admin newAdmin = new Admin();
        newAdmin.setUid(CommonUtils.makeID(18));
        newAdmin.setUsername(input_admin.getUsername().toLowerCase());
        newAdmin.setPassword(HashUtils.hashPassword(input_admin.getUsername(), input_admin.getPassword()));
        newAdmin.setState(input_admin.getState());
        newAdmin.setGoogle(input_admin.getGoogle());
        newAdmin.setGroup_id(input_admin.getGroup_id());
        newAdmin.setCreated_ip(ip);
        newAdmin.setCreated_by_uid(adminSession.getUid());
        newAdmin.setCreated_by_name(adminSession.getUName());
        adminService.save(newAdmin);
        log.info("\"添加管理员 成功 操作员：[{} - {}] 信息：[uid:{} - gk:{} - pwd:{}]\"",
                newAdmin.getUsername(), ip, newAdmin.getUid(), newAdmin.getGoogle(), newAdmin.getPassword());
        return Result.success("添加成功！");
    }

    @PostMapping("/update")
    public Result update(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String password = decParams.get("password").toString();
        String google = decParams.get("google").toString();
        String uid = decParams.get("uid").toString();
        String group_id = decParams.get("group_id").toString();
        Short state = CommonUtils.parseInt(decParams.get("state").toString()).shortValue();

        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Admin updateAdmin = new Admin();
        updateAdmin.setState(state);
        updateAdmin.setUid(uid);
        Admin admin = adminService.getById(uid);
        if(admin == null) {
            log.error("更新信息失败！ 商户:[" + admin.getUid() + "] 不存在!" );
            return Result.error("更新信息失败！");
        }
        if(admin.getUsername().equals(GlobalConsts.ChannelAdminName)
                && !adminSession.getUid().equals(uid)) {
            log.error("无法更新超级管理员");
            return Result.error("无法更新超级管理员");
        }
        if(password != null && !password.isEmpty()) {
            updateAdmin.setPassword(HashUtils.hashPassword(admin.getUsername(), password));
        }
        if(google != null && !google.isEmpty()) {
            updateAdmin.setGoogle(google);
        }
        if(group_id != null && !group_id.isEmpty()) {
            updateAdmin.setGroup_id(group_id);
        }

        adminService.update(updateAdmin);
        log.info("更新商户管理信息 操作员:[{} - {}] 信息：[uid:{} - un:{}]",
                adminSession.getAttribute(UserSession.K_USERNAME).toString(),
                CommonUtils.getIpAddress(httpRequest), admin.getUid(), admin.getUsername());
        return Result.success("增加成功！");
    }

    @GetMapping("/delete")
    public Result delete(@RequestParam String id, HttpServletRequest httpRequest, HttpSession session) {
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        Admin admin = adminService.getById(id);
        if(admin == null) {
            log.error("更新信息失败！ 商户:[" + admin.getUid() + "] 不存在!" );
            return Result.error("更新信息失败！");
        }
        if(admin.getUsername().equals(GlobalConsts.ChannelAdminName)) {
            log.error("无法删除超级管理员");
            return Result.error("无法删除超级管理员");
        }
        log.info("删除商户管理 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),id);
        adminService.deleteByUid(id);
        log.info("删除商户管理 成功 操作员：[{} - {}] 信息：[uid:{}]", adminSession.getUName(), CommonUtils.getIpAddress(httpRequest),id);
        return Result.success("删除成功！");
    }

    @PostMapping("/own")
    public Result updatePassword(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session){
        String token = httpRequest.getHeader("T");
        UserSession userSession = (UserSession)session.getAttribute(token);
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String old_psd = decParams.get("old").toString();
        String new_psd = decParams.get("new").toString();
        if(old_psd.isEmpty() || new_psd.isEmpty()) {
            return Result.error("密码不能为空!");
        }

        String uid = userSession.getUid();
        Admin admin = adminService.getById(uid);
        if(admin == null) {
            return Result.error("商户管理员不存在！");
        }
        if(admin.getState() == 0) {
            return Result.error("当前账号已禁用！");
        }
        String oldEncPassword = HashUtils.hashPassword(admin.getUsername(), old_psd);
        String newEncPassword = HashUtils.hashPassword(admin.getUsername(), new_psd);
        if(!oldEncPassword.equals(admin.getPassword())) {
            return Result.error("原始密码错误！");
        }
        if(newEncPassword.equals(admin.getPassword())) {
            return Result.success("密码修改成功！");
        }
        admin.setPassword(newEncPassword);
        channelService.updateEncByAdmin(oldEncPassword, newEncPassword);
        adminService.update(admin);
        return Result.success("密码修改成功！");
    }

    @GetMapping("/nonce")
    public Result getNonce() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return Result.success(gAuth.createCredentials().getKey());
//        return Result.success(EncDecUtils.genSecurePassword(12));
    }
}
