package com.daifubackend.api.controller.agent;

import com.daifubackend.api.pojo.Agent;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.consts.GlobalConsts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/agent/merchant")
public class AgentMerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ChannelService channelService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       String name, Short state, String channel_id, HttpSession session, HttpServletRequest request) {
        log.info("分页查询,参数:{},{},{},{}",page,page_size,name,state);
        UserSession agentSession = (UserSession) session.getAttribute(request.getHeader("T"));

        String agent_id = agentSession.getUid();
        //调用service分页查询
        PageBean pageBean = merchantService.page(page,page_size,name,state, channel_id, agent_id);
        return Result.success(pageBean);
    }

    @GetMapping("/channel/menu")
    public Result channelMenu() {
        List<Channel> channels = channelService.getMenus();
        for(Channel channel : channels) {
            if(channel.getState() == 0) {
                channel.setName(channel.getName() + "(关闭)");
            }
        }
        return Result.success(channels);
    }

    @GetMapping("/menu")
    public Result merchantMenu(HttpSession session, HttpServletRequest request) {

        UserSession agentSession = (UserSession) session.getAttribute(request.getHeader("T"));
        String agent_id = agentSession.getUid();
        List<Merchant> merchants = merchantService.getByAgentId(agent_id);
        for(Merchant merchant : merchants) {
            if(merchant.getState() == 0) {
                merchant.setName(merchant.getName() + "(关闭)");
            }
        }
        return Result.success(merchants);
    }
}
