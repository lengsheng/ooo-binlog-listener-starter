package com.ooo.binlog.core;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ooo.binlog.annotation.SyncEntity;
import com.ooo.binlog.config.MysqlHost;
import com.ooo.binlog.config.MysqlHostProfile;
import com.ooo.binlog.model.Column;
import com.ooo.binlog.processor.AbstractBinLogProcessor;
import com.ooo.binlog.processor.AbstractCustomBinLogProcessor;
import com.ooo.binlog.processor.AbstractCustomDefaultBinLogProcessor;
import com.ooo.binlog.processor.DefaultBinLogProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理器代理类
 * @Author lzh
 * @Date 5:47 下午 19/8/2020
 * @Param
 * @return
 **/
@Data
@Slf4j
public class BinLogProcessorAgency {
    private AbstractBinLogProcessor listener;
    private Map<String, Column> tableColumn;

    private String idName;
    private Class entity;
    private Class mapperClass;
    private String listenerHost;

    private String targetDatasource;
    private String database;
    private String table;

    private static final ParserConfig snakeCase;
    static {
        snakeCase = new ParserConfig();
        snakeCase.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
    }
    /*
     * 通过entity class构造监听类
     **/
    public BinLogProcessorAgency(Class<?> entity, MysqlHostProfile profile, ApplicationContext applicationContext) throws IllegalAccessException, InstantiationException {
        this.entity = entity;
        setByEntityAnnotation(entity);
        setByProfile(profile);

        BaseMapper baseMapper = (BaseMapper) applicationContext.getBean(mapperClass);
        SyncEntity syncEntity = AnnotationUtils.findAnnotation(entity, SyncEntity.class);
        Class listenerClass = syncEntity.listenerClass();

        try {
            AbstractBinLogProcessor listener = (AbstractBinLogProcessor) listenerClass.newInstance();
            if(listener instanceof AbstractCustomBinLogProcessor){
                //1.是否是自定义监听器处理器
                listener.setBaseMapper(baseMapper);
                this.listener = listener;
            }else {
                try {
                    AbstractCustomDefaultBinLogProcessor customDefaultBinLogMonitor = applicationContext.getBean(AbstractCustomDefaultBinLogProcessor.class);
                    //2.自定义默认监听器处理器
                    customDefaultBinLogMonitor.setBaseMapper(baseMapper);
                    this.listener = customDefaultBinLogMonitor;
                }catch (NoSuchBeanDefinitionException e){
                    //3.默认监听器处理器
                    DefaultBinLogProcessor binLogMonitorDefault = new DefaultBinLogProcessor();
                    binLogMonitorDefault.setBaseMapper(baseMapper);
                    this.listener = binLogMonitorDefault;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        this.listener.setProperty(this);
    }

    private void setByProfile(MysqlHostProfile profile) {
        MysqlHost mysqlHost = profile.getByNameAndThrow(this.listenerHost);
        this.database = mysqlHost.getDatabase();
        this.targetDatasource = mysqlHost.getTargetDatasource();
    }

    public void setByEntityAnnotation(Class<?> entity) {
        TableName tableName = AnnotationUtils.findAnnotation(entity,TableName.class);
        SyncEntity syncEntity = AnnotationUtils.findAnnotation(entity, SyncEntity.class);
        this.table = tableName.value();
        this.listenerHost = syncEntity.listenerHost();
        this.idName = syncEntity.id();
        this.mapperClass = syncEntity.mapperClass();
    }

    public String getEntityName(){
        return entity.getName();
    }

    public void invokeUpdate(List<Map.Entry<Serializable[], Serializable[]>> data) {
        data.forEach(row -> {
            Map<String, Serializable> before = resolver(row.getKey());
            String id = String.valueOf(before.get(idName));
            Object beforeEntity = TypeUtils.cast(row.getKey(), getEntity(), snakeCase);
            Object afterEntity = TypeUtils.cast(row.getValue(), getEntity(), snakeCase);
            listener.onUpdate(id,beforeEntity, afterEntity);
        });
    }

    public void invokeInsert(List<Serializable[]> rows) {
        rows.stream().map(this::resolver)
                .forEach(row -> {
                    String id = String.valueOf(row.get(idName));
                    Object obj = TypeUtils.cast(row, getEntity(), snakeCase);
                    listener.onInsert(id,obj);
                });
    }


    public void invokeDelete(List<Serializable[]> rows) {
        rows.stream().map(this::resolver)
                .forEach(row->{
                    String id = String.valueOf(row.get(idName));
                    Object obj = TypeUtils.cast(row, getEntity(), snakeCase);
                    listener.onDelete(id,obj);
                });
    }

    private Map<String, Serializable> resolver(Map<String, Serializable> row) {
        Map<String, Serializable> af = new HashMap<>();
        return af;
    }

    private Map<String, Serializable> resolver(Serializable[] row) {
        Map<String, Serializable> af = new HashMap<>();
        tableColumn.entrySet().stream().forEach(entry -> {
            String key = entry.getKey();
            Column column = entry.getValue();
            af.put(key, row[column.inx]);
        });
        return af;
    }
}
