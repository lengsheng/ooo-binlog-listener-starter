package com.ooo.binlog.core;

import com.ooo.binlog.annotation.SyncEntity;
import com.ooo.binlog.config.MysqlHostProfile;
import com.ooo.binlog.thread.BinlogThreadStarter;
import org.reflections.Reflections;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 入口，扫描entity生成注册监听器
 * @Author lzh
 * @Date 2:31 下午 19/8/2020
 * @Param
 * @return
 **/
public class MonitorBeanProcessor implements SmartInitializingSingleton {

    @Autowired
    private MysqlHostProfile profile;

    private ApplicationContext applicationContext;

    @Override
    public void afterSingletonsInstantiated() {

        //默认生成的监听类
        Reflections r2 = new Reflections(profile.getEntityPackage());
        Set<Class<?>> entityClass = r2.getTypesAnnotatedWith(SyncEntity.class);

        Map<String, BinLogProcessorAgency> listenerProperty = entityClass.stream()
                .map(entity-> {
                    try {
                        return new BinLogProcessorAgency(entity,profile,applicationContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(profile -> !Objects.isNull(profile))
                .collect(Collectors.toMap(BinLogProcessorAgency::getEntityName, Function.identity()));

        //根据host分组
        Map<String, List<BinLogProcessorAgency>> listeners = listenerProperty.values().stream()
                .collect(Collectors.groupingBy(BinLogProcessorAgency::getListenerHost));
        listeners.forEach((k,v) -> new BinlogThreadStarter().runThread(profile.getByNameAndThrow(k), v));
    }

    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }
}

