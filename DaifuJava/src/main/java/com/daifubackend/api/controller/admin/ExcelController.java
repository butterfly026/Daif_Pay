package com.daifubackend.api.controller.admin;

import com.daifubackend.api.pojo.PageBean;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.service.admin.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/excel")
public class ExcelController {

    @Autowired
    private MerchantService merchantService;

    /**<a href="http://demo.org:8080/emps">分页查询</a>*/
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,     //前端没给就设置默认值1 @RequestParam(defaultValue = "1")
                        @RequestParam(defaultValue = "10") Integer page_size,
                        String name, Short state) {
        log.info("分页查询,参数:{},{},{},{},{},{}",page,page_size,name,state);
        //调用service分页查询
        PageBean pageBean = merchantService.page(page,page_size,name,state, "");
        return Result.success(pageBean);
    }
}
