package com.ooo.binlog.model;

/**
 * mysql表结构信息
 * @Author lzh
 * @Date 2:34 下午 19/8/2020
 * @Param
 * @return
 **/
public class Column {
    public final String colName; // 列名
    public int inx;
    public final String dataType; // 类型

    public Column(String schema, String table, int idx, String colName, String dataType) {
        this.colName = colName;
        this.dataType = dataType;
        this.inx = idx;
    }
}