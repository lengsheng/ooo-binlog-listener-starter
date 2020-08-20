package com.ooo.binlog.config;

import lombok.Data;

@Data
public class MysqlHost {

    /**
     * 是否开启, 默认关闭
     */
    private boolean enable = false;
    /**
     * 监听名称
     */
    private String name;
    /**
     * 数据库host
     */
    private String host;
    /**
     * 数据库端口
     */
    private int port;
    /**
     * 数据库用户名
     */
    private String username;
    /**
     * 数据库密码
     */
    private String password;

    /**
     * 库名
     */
    private String database;

    /**
     * 要写入的mybatis动态数据源别名
     */
    private String targetDatasource;

}
