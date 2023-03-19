package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.entity.UserLoginRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户登录记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface UserLoginRecordService extends IService<UserLoginRecord> {

    Result getUserLoginRecord(Long userId);
}
