package com.srb.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result {
    private Integer code;
    private String message;

    private Map<String,Object> data =new HashMap<>();

    /**
     * 构造函数私有化
     */
    private Result(){

    }

    /**
     * 返回成功结果
     * @return
     */
    public static  Result ok(){
        Result result = new Result();
        result.setCode(ResponseEnum.SUCCESS.getCode());
        result.setMessage(ResponseEnum.SUCCESS.getMessage());
        return result;
    }

    /**
     * 返回失败结果
     * @return
     */
    public static Result error(){
        Result result = new Result();
        result.setCode(ResponseEnum.ERROR.getCode());
        result.setMessage(ResponseEnum.ERROR.getMessage());
        return result;
    }

    /**
     * 特定的情况
     * @return
     */
    public static Result setResult(ResponseEnum responseEnum){
        Result result = new Result();
        result.setCode(responseEnum.ERROR.getCode());
        result.setMessage(responseEnum.ERROR.getMessage());
        return result;
    }

    /**
     * 设置返回的数据
     * @param key
     * @param value
     * @return
     */

    public Result data(String key,Object value){
        this.data.put(key, value);
        return this;
    }

    public Result data(Map<String,Object> map){
        this.setData(map);
        return this;
    }

    /**
     * 设置返回特定的消息
     * @param message
     * @return
     */
    public  Result  message(String message){
        this.message=message;
        return this;
    }

    /**
     * 设置特定的状态码
     * @param code
     * @return
     */
    public  Result  code(Integer code){
        this.code = code;
        return this;
    }


}
