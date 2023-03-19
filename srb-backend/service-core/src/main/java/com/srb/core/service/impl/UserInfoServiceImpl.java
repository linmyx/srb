package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.srb.base.utils.JwtUtils;
import com.srb.common.exception.Assert;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.common.utils.MD5;
import com.srb.common.utils.RegexValidateUtils;
import com.srb.core.pojo.entity.UserAccount;
import com.srb.core.pojo.entity.UserInfo;
import com.srb.core.mapper.UserInfoMapper;
import com.srb.core.pojo.entity.UserLoginRecord;
import com.srb.core.pojo.query.UserInfoQuery;
import com.srb.core.pojo.vo.LoginVo;
import com.srb.core.pojo.vo.RegisterVO;
import com.srb.core.pojo.vo.UserIndexVO;
import com.srb.core.pojo.vo.UserInfoVo;
import com.srb.core.service.UserAccountService;
import com.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.core.service.UserLoginRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserLoginRecordService userLoginRecordService;

    /**
     * 用户注册
     * @param registerVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result register(RegisterVO registerVO) {

        String code = registerVO.getCode();
        String mobile = registerVO.getMobile();
        String password = registerVO.getPassword();
        Integer userType = registerVO.getUserType();

        //判断手机号是否为空
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        //PASSWORD_NULL_ERROR(-204, "密码不能为空"),
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        //CODE_NULL_ERROR(-205, "验证码不能为空"),
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);
        //对密码进行MD5加密
        String md5Password = MD5.encrypt(password);
        //判断验证码是否正确
        String codeGen = (String)redisTemplate.opsForValue().get("srb:sms:code:" + mobile);

        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        //判断当前手机号是否已经被注册
        QueryWrapper<UserInfo> queryWrap=new QueryWrapper();
        queryWrap.eq("mobile",mobile);
        int count = count(queryWrap);
        if (count > 0) {
            return Result.error().message("当前账号已被注册!!");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setPassword(registerVO.getPassword());
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setStatus(UserInfo.STATUS_NORMAL);
        userInfo.setHeadImg("https://i.postimg.cc/QCk5FpZb/fm.jpg");
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setName(registerVO.getMobile());
        boolean save = save(userInfo);
        if ( !save) {
            return Result.error().message("注册失败!!");
        }

        //创建用户账户记录
        UserAccount userAccount=new UserAccount();
        userAccount.setUserId(userInfo.getId());
        boolean IsUserAccount = userAccountService.save(userAccount);
        if (!IsUserAccount) {
            return Result.error().message("注册失败!!");
        }

        redisTemplate.delete("srb:sms:email:" + registerVO.getMobile());

        return Result.ok().message("注册成功!!");
    }

    @Override
    @Transactional
    public Result login(LoginVo loginVo, HttpServletRequest request) {

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        Integer userType = loginVo.getUserType();
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        //获取用户登录ip
        String ip = request.getRemoteAddr();
        //给密码进行md5加密
        String md5Password = MD5.encrypt(password);
        //用户是否存在
        QueryWrapper<UserInfo> query = new QueryWrapper<UserInfo>();
        query.eq("mobile", mobile);
        query.eq("user_type",userType);
        UserInfo userInfo = getOne(query);
        Assert.notNull(userInfo,ResponseEnum.LOGIN_MOBILE_ERROR);
        //密码是否正确
        Assert.equals(password,userInfo.getPassword(), ResponseEnum.LOGIN_PASSWORD_ERROR);
        //用户是否被禁用
        //LOGIN_DISABLED_ERROR(-210, "用户已被禁用"),
        Assert.equals(userInfo.getStatus(), UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);
        //记录用户的登录ip
        //记录登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(ip);
        boolean save = userLoginRecordService.save(userLoginRecord);
        if ( !save) {
            return Result.error().message("登录失败!!");
        }
        //生成token
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());

        //组装UserInfoVo
        UserInfoVo userInfoVo=new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        userInfoVo.setToken(token);

        return Result.ok().message("登录成功").data("row",userInfoVo);
    }

    /**
     * 后台分页获取会员列表
     * @param userInfoQuery
     * @return
     */

    @Override
    public Result getUserInfoPageList(UserInfoQuery userInfoQuery,Long page,Long limit) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        Page<UserInfo> userInfo = new Page<>(page,limit);
        Map<String, Object> map = new HashMap<>();
        if (userInfoQuery == null) {
            page(userInfo,queryWrapper);
            map.put("total",userInfo.getTotal());
            map.put("row",userInfo.getRecords());
            return Result.ok().data(map);
        }
        queryWrapper
                .eq(StringUtils.isNotBlank(userInfoQuery.getMobile()), "mobile", userInfoQuery.getMobile())
                .eq(userInfoQuery.getStatus() != null, "status", userInfoQuery.getStatus())
                .eq(userInfoQuery.getUserType() != null, "user_type", userInfoQuery.getUserType());
        page(userInfo,queryWrapper);
        map.put("total",userInfo.getTotal());
        map.put("row",userInfo.getRecords());
        return Result.ok().data(map);
    }

    /**
     * 修改用户状态
     * @param id
     * @param status
     * @return
     */
    @Override
    public Result lock(Long id, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        boolean b = updateById(userInfo);
        if (!b) {
            return Result.error().message("状态修改失败!!");
        }
        return Result.ok().message("状态修改成功!!");
    }

    /**
     * 校验手机号是否已经注册
     * @param mobile
     * @return
     */
    @Override
    public boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> queryWrap=new QueryWrapper();
        queryWrap.eq("mobile",mobile);
        int count = count(queryWrap);
        if (count > 0) {
           return true;
        }
        return false;
    }

    /**
     * 获取用户个人空间信息
     * @param userId
     * @return
     */
    @Override
    public UserIndexVO getIndexUserInfo(Long userId) {
        //用户信息
        UserInfo userInfo = getById(userId);
        //账户信息
        QueryWrapper<UserAccount> queryWrap = new QueryWrapper<>();
        queryWrap.eq("user_id",userId);
        UserAccount userAccount = userAccountService.getOne(queryWrap);

        //获取账户最后登录时间
        QueryWrapper<UserLoginRecord> userLoginRecordQueryWrapper=new QueryWrapper<>();

        userLoginRecordQueryWrapper.eq("user_id",userId).orderByDesc("id").last("limit 1");
        UserLoginRecord userLoginRecord = userLoginRecordService.getOne(userLoginRecordQueryWrapper);


        UserIndexVO userIndexVO = new UserIndexVO();
        userIndexVO.setUserId(userId);
        userIndexVO.setUserType(userInfo.getUserType());
        userIndexVO.setName(userInfo.getName());
        userIndexVO.setHeadImg(userInfo.getHeadImg());
        userIndexVO.setNickName(userInfo.getNickName());
        userIndexVO.setBindStatus(userInfo.getBindStatus());
        userIndexVO.setAmount(userAccount.getAmount());
        userIndexVO.setFreezeAmount(userAccount.getFreezeAmount());
        userIndexVO.setLastLoginTime(userLoginRecord.getCreateTime());
        return userIndexVO;
    }

    /**
     * 根据bindCode获取手机号
     * @param bindCode
     * @return
     */
    @Override
    public String getMobileByBindCode(String bindCode) {
        QueryWrapper<UserInfo> userInfoQueryWrapper=new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", bindCode);
        UserInfo userInfo = getOne(userInfoQueryWrapper);
        return userInfo.getMobile();
    }
}
