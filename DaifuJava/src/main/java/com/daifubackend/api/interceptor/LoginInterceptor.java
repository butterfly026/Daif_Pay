package com.daifubackend.api.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.daifubackend.api.pojo.Result;
import com.daifubackend.api.pojo.UserSession;
import com.daifubackend.api.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override   //目标资源方法运行前运行，返回true: 放行，放回false，不放行
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        System.out.println("preHandle  目标资源方法运行前运行(" + req.getRequestURI() + ")");


//        //1.获取请求URL
        String url = req.getRequestURI();
        log.info("请求的URL为:{}",url);

//
//        //2.判断请求头是否包含登录login，如果包含，说明是登录操作，放行
        if (url.contains("login") || url.startsWith("/merchant/api")) {
            log.info("登录操作,放行");
            return true;
        }


        //3.获取请求头中的令牌（token）.
        String jwt = req.getHeader("T");
        //4.判断令牌是否存在，如果未存在 就返回错误结果（未登录）
        if (!StringUtils.hasLength(jwt)) {
            log.info("请求头token为空,返回未登录数据");
            Result error = Result.error("NOT_LOGIN");
            //手动转换--》对象-》json   -=》阿里巴巴fastJSON
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;
        }
//        log.debug(req.getSession().getId() + " ******** " + jwt + " ");
//        Enumeration<String> attrs = req.getSession().getAttributeNames();
//        while(attrs.hasMoreElements()) {
//            String attr = attrs.nextElement();
//            log.error("{} : {}", attr, req.getSession().getAttribute(attr));
//        }
//        log.debug(" ***** ");

        UserSession session = (UserSession) req.getSession().getAttribute(jwt);


        if (session == null) {
            log.error(jwt + " session is null");
            Result error = Result.error("token");
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;
        }

        if ((session.isAdmin() && !req.getRequestURI().startsWith("/admin/")) ||
                (session.isMember() && !req.getRequestURI().startsWith("/merchant/")) ||
                (session.isAgent() && (!req.getRequestURI().startsWith("/agent/") && !req.getRequestURI().startsWith("/merchant/")))) {
            log.error(jwt + " invalid url access!");
            Result error = Result.error("token");
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;

        }


        //5.解析token 如果解析失败就返回错误结果（未登录）
        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败,返回未登录错误信息");
            Result error = Result.error("NOT_LOGIN");
            //手动转换--》对象-》json   -=》阿里巴巴fastJSON
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;
        }

        //6.放行
//        log.info("令牌合法,放行");
        return true;
    }

    @Override   //目标资源方法运行后运行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("postHandle  标资源方法运行后运行");
    }

    @Override   //视图渲染完毕后运行，最后运行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        System.out.println("afterCompletion  视图渲染完毕后运行，最后运行");
    }
}