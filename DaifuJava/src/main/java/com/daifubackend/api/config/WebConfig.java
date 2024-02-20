package com.daifubackend.api.config;

import com.daifubackend.api.interceptor.LoginInterceptor;
import com.daifubackend.api.utils.CustomCacheManager;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Configuration //配置类
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 注册自己定义的拦截器LoginInterceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)   //注册拦截器
                .addPathPatterns("/**")             //拦截全部路径    /*是一级路径/**是全部路径
                .excludePathPatterns("/admin/member/login", "/merchant/member/login", "/agent/member/login", "/merchant/api/create",
                        "/merchant/api/cb", "/merchant/api/balance", "/merchant/api/order");     //放行/login路径

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(stringConverter);
//        // Add other converters as needed
//        WebMvcConfigurer.super.configureMessageConverters(converters);
    }

    @Bean
    public AntiSamy antiSamy() throws Exception {
        Policy policy = Policy.getInstance(Objects.requireNonNull(getClass().getResourceAsStream("/antisamy/antisamy.xml")));
        return new AntiSamy(policy);
    }

    @Bean
    public CacheManager cacheManager() {
        return new CustomCacheManager();
    }
}
