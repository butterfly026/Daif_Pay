package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.service.TransactionService;
import com.daifubackend.api.service.admin.MerchantService;
import com.daifubackend.api.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/admin/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       String st, String et, String order_id, Integer cash_type, Long merchant_id) {
        log.info("分页查询,参数:{},{},{},{},{},{}",page,page_size,st,et, order_id, cash_type);
        //调用service分页查询
        HashMap<String, Object> params = new HashMap<>();
        if(st != null && !st.isEmpty())
            params.put("st", CommonUtils.strtotime(st));
        if(et != null && !et.isEmpty())
            params.put("et", CommonUtils.strtotime(et));
        params.put("order_id", order_id);
        params.put("cash_type", cash_type);
        params.put("merchant_id", merchant_id);
        PageBean pageBean = transactionService.page(page,page_size, params);
        return Result.success(pageBean);
    }

    @GetMapping("/menu")
    public Result menu() {
        return Result.success(transactionService.getMenu());
    }



}
