package com.ooo.binlog.processor;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.ooo.binlog.utils.MD5Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认监听器实现
 * @Author lzh
 * @Date 11:57 上午 19/8/2020
 * @Param
 * @return
 **/
@Slf4j
@Data
public class DefaultBinLogProcessor extends AbstractDefaultBinLogProcessor {


    public DefaultBinLogProcessor() {}

    @Override
    public void onUpdate(String id, Object beforeEntity, Object afterEntity) {
        log.debug("update tableName:{} ID:{} mybatis db:{}",property.getTable(),id,property.getTargetDatasource());
        try {
            DynamicDataSourceContextHolder.push(property.getTargetDatasource());
            Object targetRecord = baseMapper.selectById(id);
            if(targetRecord != null && !MD5Util.equles(afterEntity, targetRecord)) {
                baseMapper.updateById(afterEntity);
            }
            DynamicDataSourceContextHolder.clear();
        } catch (Exception e) {
            log.error("ID 为 {} 的条目数据变更，写入缓存", id,e);
            // 写入redis
        }
    }

    @Override
    public void onInsert(String id, Object entity) {
        log.info("插入ID为 {} 的数据", id);
        log.debug("insert tableName:{} ID:{} mybatis db:{}",property.getTable(),id,property.getTargetDatasource());
        try {
            Object targetRecord = baseMapper.selectById(id);
            if(targetRecord != null) {
                baseMapper.deleteById(id);
            }
        } catch (Exception e) {
            log.error("插入ID为 {} 的数据失败，写入缓存", id,e);
            // 写入redis
        }
    }

    @Override
    public void onDelete(String id, Object entity) {
        log.info("删除ID为 {} 的数据", id);
        log.debug("delete tableName:{} ID:{} mybatis db:{}",property.getTable(),id,property.getTargetDatasource());
        try {
            Object targetRecord = baseMapper.selectById(id);
            if(targetRecord != null) {
                baseMapper.updateById(entity);
            }
        } catch (Exception e) {
            log.error("插入ID为 {} 的数据失败，写入缓存", id,e);
            // 写入redis
        }
    }

//    @Override
//    public void onInsert(Map<String, Serializable> data) {
//        Long id = (Long) after.get(property.getIdName());
//
//        Object afterEntity = TypeUtils.cast(after, property.getEntity(), snakeCase);
//        log.debug("update tableName:{} ID:{} mybatis db:{}",property.getTable(),id,property.getTargetDatasource());
//        log.info("插入ID为 {} 的数据", data.getId());
//        try {
//
//            ChannelEntity tgtOtaTgtOtaChannelEntity = tgtOtaChannelMapper.selectById(data.getId());
//            if(tgtOtaTgtOtaChannelEntity == null) {
//                tgtOtaTgtOtaChannelEntity = BeanUtil.copyProperties(data, ChannelEntity.class);
//                tgtOtaChannelMapper.insert(tgtOtaTgtOtaChannelEntity);
//            }
//
//        } catch (Exception e) {
//            log.error("插入ID为 {} 的数据失败，写入缓存", data.getId(),e);
//            // 写入redis
//        }
//    }


}
