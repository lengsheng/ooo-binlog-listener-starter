package com.ooo.binlog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Optional;

/**
 * @Author: chenhao
 * @Description:
 * @Date: Created in 15:35 2020-08-03
 * @Modified By:
 */

@Getter
@Setter
@ConfigurationProperties(prefix = MysqlHostProfile.PREFIX)
public class MysqlHostProfile {

    public static final String PREFIX = "ooo.binlog";

    private String[] entityPackage;

    private List<MysqlHost> hosts;

    public Optional<MysqlHost> queryByName(String name) {
        return hosts.stream().filter(v -> name.equals(v.getName())).findAny();
    }

    public MysqlHost getByNameAndThrow(String name) {
        return queryByName(name).orElseThrow(() -> new RuntimeException("未配置名为 "+name+" 的 binlog 连接信息"));
    }
}
