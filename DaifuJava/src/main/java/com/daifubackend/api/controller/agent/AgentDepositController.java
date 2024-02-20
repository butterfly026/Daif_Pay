package com.daifubackend.api.controller.agent;

import com.daifubackend.api.pojo.Deposit;
import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.pojo.admin.Channel;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.DepositService;
import com.daifubackend.api.service.ProcedureService;
import com.daifubackend.api.service.admin.ChannelService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.daifubackend.api.utils.consts.Banks;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/agent/deposit")
public class AgentDepositController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ChannelService channelService;
    
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       @RequestParam HashMap<String, Object> params,
                       HttpServletRequest httpRequest, HttpSession session) {
        //调用service分页查询
        String token = httpRequest.getHeader("T");
        UserSession agentSession = (UserSession)session.getAttribute(token);
        String agent_id = agentSession.getUid();
        params.put("agent_id", agent_id);
        PageBean pageBean = depositService.page(page,page_size, params);
        return Result.success(pageBean);
    }


}
