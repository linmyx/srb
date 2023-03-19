package com.srb.core.service;

import com.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.srb.core.pojo.vo.UserBindVo;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface UserBindService extends IService<UserBind> {

    String authBind(UserBindVo userBindVo, Long userId);

    void notify(Map<String, Object> map);

    String getBindCodeByUserId(Long userId);
}
