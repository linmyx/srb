package com.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.common.result.Result;
import com.srb.core.pojo.entity.UserLoginRecord;
import com.srb.core.mapper.UserLoginRecordMapper;
import com.srb.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    /**
     * 获取会员前50次的登录日志
     * @param userId
     * @return
     */
    @Override
    public Result getUserLoginRecord(Long userId) {
        QueryWrapper<UserLoginRecord> query = new QueryWrapper<UserLoginRecord>();
        query.eq("user_id", userId);
        query.orderByDesc("id");
        query.last("limit 50");
        List<UserLoginRecord> list = list(query);
        return Result.ok().data("rws",list);
    }
}
