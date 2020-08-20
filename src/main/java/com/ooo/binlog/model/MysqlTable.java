package com.ooo.binlog.model;

import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import lombok.Getter;

/**
 * mysql表信息
 * @Author lzh
 * @Date 2:34 下午 19/8/2020
 * @Param
 * @return
 **/
@Getter
public class MysqlTable {

    private long id;
    private String database;
    private String table;

    public MysqlTable(TableMapEventData data) {
        this.id = data.getTableId();
        this.database = data.getDatabase();
        this.table = data.getTable();
    }
}
