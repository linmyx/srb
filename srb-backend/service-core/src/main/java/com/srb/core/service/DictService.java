package com.srb.core.service;

import com.srb.common.result.Result;
import com.srb.core.pojo.dto.ExcelDictDTO;
import com.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author Helen
 * @since 2023-03-05
 */
public interface DictService extends IService<Dict> {

    void importDate(InputStream importDate);

    List<ExcelDictDTO> getDictData();

    Result getDictParentById(long parentId);

    Result findByDictCode(String dictCode);
    String getDictCodeAndValue(String dictCode,Integer value);
}
