package com.daifubackend.api.controller.merchant;

import com.daifubackend.api.pojo.*;
import com.daifubackend.api.service.TransactionService;
import com.daifubackend.api.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/merchant/transaction")
public class MerchantTransactionController {

    @Autowired
    private TransactionService transactionService;


    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                       @RequestParam(defaultValue = "10") Integer page_size,
                       @RequestParam(name="st", required = false) String st, @RequestParam(name="et", required = false) String et,
                       @RequestParam(name="order_id", required = false) String order_id, @RequestParam(name="merchant_id", required = false) String merchant_id,
                       @RequestParam(name="cash_type", required = false) Integer cash_type) {
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


}
