package com.ooo.binlog.processor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ooo.binlog.core.BinLogProcessorAgency;

/**
 * 监听器 抽象类，提供一些公共属性、方法
 * @Author lzh
 * @Date 2:30 下午 19/8/2020
 * @Param
 * @return
 **/
public abstract class AbstractBinLogProcessor implements BinLogProcessor {

    protected BinLogProcessorAgency property = null;
    protected BaseMapper baseMapper;

    public String getDbTable() {
        return property.getDatabase() + "." + property.getTable();
    }

    public void setProperty(BinLogProcessorAgency listenerProperty) {
        this.property = listenerProperty;
    }

    public void setBaseMapper(BaseMapper baseMapper) {
        this.baseMapper = baseMapper;
    }


    public String getDatabase() {
        return this.property.getDatabase();
    }

    public String getTable() {
        return this.property.getTable();
    }
}
