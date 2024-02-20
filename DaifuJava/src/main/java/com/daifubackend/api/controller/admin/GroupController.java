package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Group;
import com.daifubackend.api.service.admin.GroupService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/list")
    public Result list() {
        return Result.success(groupService.list());
    }

    @GetMapping("/priv")
    public Result getPriv() {
        return Result.success(groupService.getPriv());
    }

    @GetMapping("/delete")
    public Result getPriv(@RequestParam String gid) {
        groupService.deleteById(gid);
        return Result.success("删除成功!");
    }

    @PostMapping(path="/insert", consumes = "application/x-www-form-urlencoded")
    public Result insert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        Integer code = CommonUtils.parseInt(decParams.get("code").toString());

        UserSession adminSession = (UserSession) session.getAttribute(httpRequest.getHeader("T"));
        if (adminSession == null){
            return  Result.error("Session expired");
        }
        if(code == null || code == 0) {
            return Result.error("验证码不能为空");
        } else {
            if(!EncDecUtils.verifyGoogle2fa(adminSession.getAttribute(UserSession.K_GOOGLEKEY).toString(), code)) {
                return Result.error("验证码错误");
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        Group group = mapper.convertValue(decParams, Group.class);
        group.setGid(CommonUtils.makeID(19));
        groupService.save(group);
        return Result.success("添加成功!");
    }

    @PostMapping(path="/update", consumes = "application/x-www-form-urlencoded")
    public Result update(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        Integer code = CommonUtils.parseInt(decParams.get("code").toString());
        UserSession adminSession = (UserSession) session.getAttribute(httpRequest.getHeader("T"));
        if (adminSession == null){
            return  Result.error("Session expired");
        }
        if(code == null || code == 0) {
            return Result.error("验证码不能为空");
        } else {

            if(!EncDecUtils.verifyGoogle2fa(adminSession.getAttribute(UserSession.K_GOOGLEKEY).toString(), code)) {
                return Result.error("验证码错误");
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        Group group = mapper.convertValue(decParams, Group.class);
        groupService.update(group);
        return Result.success("更新成功");
    }
}
