package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.Member;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Admin;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.service.admin.AdminService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.GlobalConsts;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/channel")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private AdminService adminService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       String name, Short state, HttpSession session, HttpServletRequest request) {
        log.info("分页查询,参数:{},{},{},{},{},{}",page,page_size,name,state);
        UserSession adminSession = (UserSession) session.getAttribute(request.getHeader("T"));
        boolean isValidAdmin = adminSession.getUName().equals(GlobalConsts.ChannelAdminName);
        //调用service分页查询
        PageBean pageBean = channelService.page(page,page_size,name,state, isValidAdmin);
        return Result.success(pageBean);
    }

    @PostMapping(path = "/update", consumes = "application/x-www-form-urlencoded")
    public Result update(@RequestBody String requestBody, HttpSession session, HttpServletRequest request) {

        Map decParams = EncDecUtils.decodePostParam(requestBody);

        UserSession adminSession = (UserSession) session.getAttribute(request.getHeader("T"));
        boolean isValidAdmin = adminSession.getUName().equals(GlobalConsts.ChannelAdminName);
        if(!isValidAdmin) {
            return Result.error("你无权进行此操，请联系超级管理员。");
        }
        ObjectMapper mapper = new ObjectMapper();
        Channel channel = mapper.convertValue(decParams, Channel.class);
        if(channel.getShortname() != null && !channel.getShortname().isEmpty()) {
            HashMap<String, Object> hMap = new HashMap<>();
            hMap.put("shortname", channel.getShortname());
            hMap.put("isValidAdmin", true);
            List<Channel> channels = channelService.getChannelByCondition(hMap);
            if(channels != null && channels.size() > 0) {
                for(Channel ch : channels) {
                    if(!ch.getId().equals(channel.getId())) {
                        return Result.error("通道短名 [" + channel.getShortname() + "] 已经存在!");
                    }
                }
            }
        }
        if(channel.getPpk() == null || channel.getPpk().isEmpty()) {
            return Result.error("商户秘钥不能为空！");
        }

        if(channel.getPpk().length() <= 6) {
            channel.setPpk(null);
        }

        log.info("更新通道信息 通道：[{} - {} - {}]  操作员：[{} - {}]", channel.getId(), channel.getName(), channel.getShortname(), adminSession.getUName(), CommonUtils.getIpAddress(request) );
        channelService.update(channel);
        log.info("更新通道信息 <失败> 操作员：[{} - {}]", adminSession.getUName(), CommonUtils.getIpAddress(request) );
        return Result.success("更新失败，[参数] 错误或 [通道重名]！");
    }

    @PostMapping(path="/insert", consumes = "application/x-www-form-urlencoded")
    public Result insert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {

        Map decParams = EncDecUtils.decodePostParam(requestBody);
        ObjectMapper mapper = new ObjectMapper();
        Channel channel = mapper.convertValue(decParams, Channel.class);
        UserSession adminSession = (UserSession) session.getAttribute(httpRequest.getHeader("T"));
        boolean isValidAdmin = adminSession.getUName().equals(GlobalConsts.ChannelAdminName);
        if(!isValidAdmin) {
            return Result.error("你无权进行此操，请联系超级管理员。");
        }
        channel.setId(CommonUtils.makeID(19));
        channel.setPpk(CommonUtils.encryptPPK(channel.getPpk(), CommonUtils.gkeyCryptKey));

        Admin channelAdmin = adminService.getByUsername(GlobalConsts.ChannelAdminName);
        log.info("添加通道信息 通道：[{} - {}]  操作员：[{} - {}]", channel.getName(), channel.getShortname(), adminSession.getUName(), CommonUtils.getIpAddress(httpRequest) );
        if(channelAdmin == null) {
            log.error("Can not find channel admin");
            return Result.error("增加失败!");
        }
        HashMap<String, Object> map = new HashMap<>();
        if(channel.getName() != null && !channel.getName().isEmpty()) {
            map.put("name", channel.getName());
            map.put("isValidAdmin", true);
            List<Channel> oldchannels = channelService.getChannelByCondition(map);
            if(oldchannels != null && oldchannels.size() > 0) {
                return Result.error("通短名 [" + channel.getName() + "] 已经存在!");
            }
        }
        if(channel.getShortname() != null && !channel.getShortname().isEmpty()) {
            map.clear();
            map.put("shortname", channel.getShortname());
            map.put("isValidAdmin", true);
            List<Channel> oldchannels = channelService.getChannelByCondition(map);
            if(oldchannels != null && oldchannels.size() > 0) {
                return Result.error("通道短名 [" + channel.getShortname() + "] 已经存在!");
            }
        }
        channelService.save(channel);
        log.info("添加通道 成功 通道：[{} - {}]  操作员：[{} - {}]", channel.getName(), channel.getShortname(), adminSession.getUName(), CommonUtils.getIpAddress(httpRequest) );
        return Result.success("增加成功!");
    }


    @GetMapping("/menu")
    public Result menu() {

        List<Channel> channels = channelService.getMenus();
        for(Channel channel : channels) {
            if(channel.getState() == 0) {
                channel.setName(channel.getName() + "(关闭)");
            }
        }
        return Result.success(channels);
    }

    @PostMapping("/disabled")
    public Result disabled() {
        return Result.success();
    }

    @GetMapping("/close")
    public Result closeAllChannel(@RequestParam(name = "state", required = false) Short state) {

        return Result.success(1);
    }

    @GetMapping("/delete")
    public Result delete(@RequestParam String id, HttpSession session, HttpServletRequest request) {
        UserSession adminSession = (UserSession) session.getAttribute(request.getHeader("T"));
        boolean isValidAdmin = adminSession.getUName().equals(GlobalConsts.ChannelAdminName);
        if(!isValidAdmin) {
            return Result.error("你无权进行此操，请联系超级管理员。");
        }
        channelService.deleteById(id);
        log.info("删除通道成功 通道：[{}]  操作员：[{} - {}]", id, adminSession.getUName(), CommonUtils.getIpAddress(request) );
        return Result.success("删除成功!");
    }

    @GetMapping("/balance")
    public Result getBalance(@RequestParam String id, HttpSession session, HttpServletRequest request) {
        UserSession adminSession = (UserSession) session.getAttribute(request.getHeader("T"));
        Channel channel = channelService.getById(id);
        if(channel == null) {
            return Result.error("该通道不存在!");
        }
        String ppk = CommonUtils.decryptPPK(channel.getPpk(), CommonUtils.ppkCryptKey);
        channel.setPpk(ppk);
//        list($retCode,$msg,$balance) = Functions::RequireChannelAndCall($shortname,"balance",$params);
//        if($retCode==0){
//            $this->responser->balanceResponse(true,$balance);
//            exit;
//        }
//        $this->responser->balanceResponse(false,"查不到余额:".$msg);
        return Result.success();
    }

}
