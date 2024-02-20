package com.daifubackend.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 请求响应码
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    private Integer status;//响应码，1 代表成功; 0 代表失败
    private Object data; //返回的数据

    //增删改 成功响应
    public static Result success(){
        return new Result(1,null);
    }
    //查询 成功响应
    public static Result success(Object data){
        return new Result(1,data);
    }
    public static Result success0(){
        return new Result(0,null);
    }
    public static Result success0(Object data){
        return new Result(0,data);
    }
    //失败响应
    public static Result error(String msg){
        return new Result(0,msg);
    }
    public static Result error(Object data){
        return new Result(0,data);
    }
    public static Result errorWithMsg(String msg){
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("code", 2);
        resMap.put("msg", msg);
        return new Result(0,resMap);
    }

    public static Result error1(String msg){
        return new Result(1,msg);
    }
    public static Result error1(Object data){
        return new Result(1,data);
    }
    public static Result errorWithMsg1(String msg){
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("code", 2);
        resMap.put("msg", msg);
        return new Result(1,resMap);
    }
}
