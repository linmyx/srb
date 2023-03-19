package com.srb.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.core.enums.UserBindEnum;
import com.srb.core.hfb.FormHelper;
import com.srb.core.hfb.HfbConst;
import com.srb.core.hfb.RequestHelper;
import com.srb.core.pojo.entity.UserBind;
import com.srb.core.mapper.UserBindMapper;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.pojo.vo.UserBindVo;
import com.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoService userInfoService;



    @Override
    public String authBind(UserBindVo userBindVo, Long userId) {

//        //首先判断当前信息是否已经被绑定
        QueryWrapper<UserBind> queryWrapper = new QueryWrapper<>();
        queryWrapper .eq("id_card", userBindVo.getIdCard());
        queryWrapper.ne("user_id", userId);
        UserBind userBind = getOne(queryWrapper);
        //USER_BIND_IDCARD_EXIST_ERROR(-301, "身份证号码已绑定"),
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

//        //查询用户绑定信息
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        userBind = getOne(queryWrapper);
//
//        //判断是否有绑定记录
        if(userBind == null) {
            //如果未创建绑定记录，则创建一条记录
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVo, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            save(userBind);
        } else {
            //曾经跳转到托管平台，但是未操作完成，此时将用户最新填写的数据同步到userBind对象
            BeanUtils.copyProperties(userBindVo, userBind);
            updateById(userBind);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard",userBindVo.getIdCard());
        paramMap.put("personalName", userBindVo.getName());
        paramMap.put("bankType", userBindVo.getBankType());
        paramMap.put("bankNo", userBindVo.getBankNo());
        paramMap.put("mobile", userBindVo.getMobile());
        paramMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));
        //生成动态表单
        String formStr=FormHelper.buildForm(HfbConst.USERBIND_URL,paramMap);

        return formStr;
    }

    @Transactional
    @Override
    public void notify(Map<String, Object> map) {

        String bindCode = (String) map.get("bindCode");
        String agentUserId = (String) map.get("agentUserId");

        QueryWrapper<UserBind> queryWrap = new QueryWrapper<>();
        UserBind userBind = getOne(queryWrap.eq("user_id", agentUserId));

        //更新用户绑定表
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        updateById(userBind);

        //更新用户表
        UserInfo userInfo = userInfoService.getById(agentUserId);
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfoService.updateById(userInfo);
    }

    @Override
    public String getBindCodeByUserId(Long userId) {
        QueryWrapper<UserBind> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("user_id",userId);
        UserBind userBind= getOne(objectQueryWrapper);
        return userBind.getBindCode();
    }
}
