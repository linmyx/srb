package com.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.srb.core.mapper.DictMapper;
import com.srb.core.pojo.dto.ExcelDictDTO;
import com.srb.core.pojo.entity.Dict;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {


    //设计常量值，用来确定多少数据时进行存储一次
    private static final int BATCH_COUNT = 5;
    List<ExcelDictDTO> list = new ArrayList();

    private DictMapper dictMapper;


    public ExcelDictDTOListener(DictMapper dictMapper){
        this.dictMapper = dictMapper;
    }


    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {
        list.add(excelDictDTO);
        if (list.size() >= BATCH_COUNT){
            //使用mapper层的service方法
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //当最后剩余的记录数不足常量时，我们最后一次性进行保存
        saveData();
    }

    public void saveData(){
        //使用mapper层的service方法  save list 批量进行存储
        dictMapper.insertBatch(list);
        list.clear();
    }
}
