package com.srb.core.mapper;

import com.srb.core.pojo.dto.ExcelDictDTO;
import com.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface DictMapper extends BaseMapper<Dict> {
    void insertBatch(List<ExcelDictDTO> list);
}
