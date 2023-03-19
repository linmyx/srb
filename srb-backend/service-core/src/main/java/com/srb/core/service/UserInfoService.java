package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.query.UserInfoQuery;
import com.srb.core.pojo.vo.LoginVo;
import com.srb.core.pojo.vo.RegisterVO;
import com.srb.core.pojo.vo.UserIndexVO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface UserInfoService extends IService<UserInfo> {

    Result register(RegisterVO registerVO);

    Result login(LoginVo loginVo, HttpServletRequest request);

    Result getUserInfoPageList(UserInfoQuery userInfoQuery,Long page,Long limit);

    Result lock(Long id, Integer status);

    boolean checkMobile(String mobile);

    UserIndexVO getIndexUserInfo(Long userId);

    String getMobileByBindCode(String bindCode);
}
