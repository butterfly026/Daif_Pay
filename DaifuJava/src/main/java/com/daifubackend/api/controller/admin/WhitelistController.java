package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.*;
import com.daifubackend.api.pojo.admin.Merchant;
import com.daifubackend.api.service.ApiWhitelistService;
import com.daifubackend.api.service.MerchantWhitelistService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import com.daifubackend.api.utils.EncDecUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/whitelist")
public class WhitelistController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantWhitelistService merchantWhitelistService;

    @Autowired
    private ApiWhitelistService apiWhitelistService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/api/list")
    public Result apiList(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                        @RequestParam(defaultValue = "10") Integer page_size,
                        Long merchant_id, String ip) {
        log.info("分页查询,参数:{},{},{},{}",page,page_size,merchant_id,ip);
        //调用service分页查询
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("merchant_id", merchant_id);
        parameters.put("ip", ip);
        PageBean pageBean = apiWhitelistService.page(page,page_size,parameters);
        return Result.success(pageBean);
    }

    @GetMapping("/api/delete")
    public Result apiDelete(String id) {
        WhiteApi api = apiWhitelistService.getDataByUid(id);
        if(api == null) {
            log.error("API白名单 [$id] 不存在!");
            return Result.error("商户 [" + id + "] 不存在!");
        }
        String merchant_id = api.getMerchant_id();
        String ip = api.getIp();
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("删除API白名单 商户:[{}] 不存在!", merchant_id);
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }
        log.info("删除API白名单 商户:[$merchant_id - $merchant_name] IP:[$ip] 操作员：[$uname]" );
        apiWhitelistService.delete(id);
        return Result.success("删除成功!");
    }

    @PostMapping("/api/insert")
    public Result apiInsert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String code = decParams.get("code").toString();
        String ip = decParams.get("ip").toString();

        String merchant_id = id;
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        if(adminSession.getGoogleKey() == null || adminSession.getGoogleKey().isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }
        if(!EncDecUtils.verifyGoogle2fa(adminSession.getGoogleKey(), Integer.parseInt(code))) {
            return Result.error("验证码错误");
        }

        int nCnt = apiWhitelistService.countByMerchantId(merchant_id);
        if(nCnt > 100) {
            return Result.error("超过最大IP白名单数");
        }

        List<WhiteApi> tmp = apiWhitelistService.getByMerchantIp(merchant_id, ip);
        if(tmp != null && tmp.size() > 0) {
            return Result.error("IP地址不能重复添加！");
        }

        WhiteApi newApi = new WhiteApi();
        newApi.setId(CommonUtils.makeID(19));
        newApi.setIp(ip);
        newApi.setMerchant_id(merchant_id);
        newApi.setCreated_at((int)(System.currentTimeMillis() / 1000));
        newApi.setCreated_by_uid(adminSession.getUid());
        newApi.setCreated_by_name(adminSession.getUName());

        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("添加API白名单 商户:[$merchant_id] 不存在!" );
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }
        log.info("添加API白名单 <发起>商户:[$merchant_id - $merchant_name] IP:[$ip] 操作员：[$uname]");
        apiWhitelistService.add(newApi);
        return Result.success("增加成功!");
    }

    @GetMapping("/merchant/list")
    public Result merchantList(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                               @RequestParam(defaultValue = "10") Integer page_size,
                               String merchant_id, String ip) {
        log.info("分页查询,参数:{},{},{},{}", page, page_size, merchant_id, ip);
        //调用service分页查询
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("merchant_id", merchant_id);
        parameters.put("ip", ip);
        PageBean pageBean = merchantWhitelistService.page(page, page_size, parameters);
        return Result.success(pageBean);
    }

    @GetMapping("/merchant/delete")
    public Result merchantDelete(String id, HttpServletRequest httpRequest, HttpSession session) {
        MerchantWhitelist merchantWhitelist = merchantWhitelistService.getDataByUid(id);
        if(merchantWhitelist == null) {
            log.error("删除商户白名单 商户 [$id] 不存在!");
            return Result.error("商户 [" + id + "] 不存在!");
        }
        String merchant_id = merchantWhitelist.getMerchant_id();
        String ip = merchantWhitelist.getIp();
        Merchant merchant = merchantService.getById(merchant_id);
        if(merchant == null) {
            log.error("删除API白名单 商户:[$merchant_id] 不存在!");
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }
        log.info("删除API白名单 商户:[$merchant_id - $merchant_name] IP:[$ip] 操作员：[$uname]" );
        merchantWhitelistService.delete(id);
        return Result.success("删除成功!");
    }

    @PostMapping("/merchant/insert")
    public Result merchantInsert(@RequestBody String requestBody, HttpServletRequest httpRequest, HttpSession session) {
        Map decParams = EncDecUtils.decodePostParam(requestBody);
        String id = decParams.get("id").toString();
        String code = decParams.get("code").toString();
        String ip = decParams.get("ip").toString();
        String token = httpRequest.getHeader("T");
        UserSession adminSession = (UserSession)session.getAttribute(token);
        log.info("商户帐变 商户：[$merchant_id] 操作员：[$uname] 金额：[$amount]");
        Merchant merchant = merchantService.getById(id);
        String merchant_id = id;
        if(merchant == null) {
            log.error("添加商户白名单 商户:[" + merchant_id + "] 不存在!" );
            return Result.error("商户 [" + merchant_id + "] 不存在!");
        }
        if(adminSession.getGoogleKey() == null || adminSession.getGoogleKey().isEmpty()) {
            return Result.error("请绑定动态密码的密钥");
        }
        if(!EncDecUtils.verifyGoogle2fa(adminSession.getGoogleKey(), Integer.parseInt(code))) {
            return Result.error("验证码错误");
        }
        int ncnt = merchantWhitelistService.countByMerchantId(merchant_id);
        if(ncnt > 100) {
            log.error("超过最大IP白名单数");
            return Result.error("超过最大IP白名单数");
        }
        MerchantWhitelist merchantWhitelist = merchantWhitelistService.getByMerchantIp(merchant_id, ip);
        if(merchantWhitelist != null) {
            return Result.error("IP地址不能重复添加！");
        }
        MerchantWhitelist merchantWhitelist1 = new MerchantWhitelist();
        merchantWhitelist1.setId(CommonUtils.makeID(19));
        merchantWhitelist1.setIp(ip);
        merchantWhitelist1.setMerchant_id(merchant_id);
        merchantWhitelist1.setCreated_at((int)(System.currentTimeMillis() / 1000));
        merchantWhitelist1.setCreated_by_uid(adminSession.getUid());
        merchantWhitelist1.setCreated_by_name(adminSession.getUName());

        merchantWhitelistService.add(merchantWhitelist1);
        log.info("添加商户白名单 成功 商户:[$merchant_id - $merchant_name] IP:[$ip] 操作员：[$uname]");
        return Result.success("增加成功!");
    }
}
