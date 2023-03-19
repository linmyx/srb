package com.srb.core.controller.api;

import com.alibaba.fastjson.JSON;
import com.srb.base.utils.JwtUtils;
import com.srb.common.result.Result;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.pojo.vo.UserBindVo;
import com.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Api(tags = "账户绑定接口")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {

    @Resource
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public Result authBind(@RequestBody UserBindVo userBindVo, HttpServletRequest request){

        //从header中获取token，并对token进行校验，确保用户已经登录，并从token当中获取userId
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        //根据userId做账户绑定,最终生成一个动态表单的字符串
        String formStr=userBindService.authBind(userBindVo,userId);
        return Result.ok().data("formStr", formStr);
    }

    @ApiOperation(value = "账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){

        //汇付宝向尚融宝回调时携带的请求参数
        Map<String, Object> map = RequestHelper.switchMap(request.getParameterMap());

        boolean signEquals = RequestHelper.isSignEquals(map);
        if (!signEquals) {
            log.error("用户账号绑定异步回调签名错误：" + JSON.toJSONString(map));
            return "fail";
        }
        //修改绑定状态
       userBindService.notify(map);
        return "success";
    }

}
