package com.ooo.binlog;


import com.ooo.binlog.config.MysqlHostProfile;
import com.ooo.binlog.core.MonitorBeanProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(MysqlHostProfile.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class BinlogListenerConfiguration {

    @Bean
    public MonitorBeanProcessor getBinLogBeanProcessor(ApplicationContext applicationContext){
        MonitorBeanProcessor processor = new MonitorBeanProcessor();
        processor.setApplicationContext(applicationContext);
        return processor;
    }
}
