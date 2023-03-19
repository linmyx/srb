package com.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srb.common.exception.BusinessException;
import com.srb.common.result.ResponseEnum;
import com.srb.common.result.Result;
import com.srb.core.listener.ExcelDictDTOListener;
import com.srb.core.pojo.dto.ExcelDictDTO;
import com.srb.core.pojo.entity.Dict;
import com.srb.core.mapper.DictMapper;
import com.srb.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    @Resource
    private RedisTemplate redisTemplate;

    /**
     * excel文件的读取
     * @param importDate
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDate(InputStream importDate) {
        EasyExcel.read(importDate, ExcelDictDTO.class,new ExcelDictDTOListener(baseMapper)).sheet().doRead();
    }

    @Override
    public List<ExcelDictDTO> getDictData() {

        List<Dict> dicts = baseMapper.selectList(null);
        //创建excel对应的实体类
        List<ExcelDictDTO> excelDictDTOList=new ArrayList<ExcelDictDTO>(dicts.size());

        dicts.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    @Override
    public Result getDictParentById(long parentId) {

        List<Dict> dictList=null;
        try {
            //首先查询数据是否存在redis当中
            dictList = (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);
            //如果存在redis当中，直接返回
            if (dictList.size() > 0){
                return Result.ok().data("row",dictList);
            }
        } catch (Exception e) {
            log.error("redis获取异常!!"+ e.getMessage());
        }
        QueryWrapper<Dict> queryWrap=new QueryWrapper<Dict>();
        queryWrap.eq("parent_id", parentId);
        dictList = list(queryWrap);
        //填充hashChildren字段
        dictList.forEach(dict -> {
            boolean hasChildren = this.isHasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        try {
            redisTemplate.opsForValue().set("srb:core:dictList:" + parentId,dictList);
        } catch (Exception e) {
            log.error("redis存储异常!!"+ e.getMessage());
        }
        return Result.ok().data("row",dictList);
    }

    /**
     * 前端根据dictCode获取字典列表
     * @param dictCode
     * @return
     */
    @Override
    public Result findByDictCode(String dictCode) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = getOne(dictQueryWrapper);
        Result dictParentById = this.getDictParentById(dict.getId());
        Map<String, Object> data = dictParentById.getData();
        List<Dict> dictList = (List<Dict>) data.get("row");
        return Result.ok().data("row",dictList);
    }

    /**
     * 根据dictCode和id来获取dict信息
     * @param dictCode
     * @param value
     * @return
     */
    @Override
    public String getDictCodeAndValue(String dictCode, Integer value) {

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<Dict>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict parentDict = baseMapper.selectOne(dictQueryWrapper);

        if(parentDict == null) {
            return "";
        }

        dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper
                .eq("parent_id", parentDict.getId())
                .eq("value", value);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);

        if(dict == null) {
            return "";
        }

        return dict.getName();
    }


    /**
     * 判断当前id节点下是否有子节点
     * @return
     */
    private boolean isHasChildren(Long id){
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id",id);
        int count = count(dictQueryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

}
