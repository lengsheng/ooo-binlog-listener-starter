package com.ooo.binlog.thread;

import com.alibaba.fastjson.JSON;
import com.ooo.binlog.config.MysqlHost;
import com.ooo.binlog.core.BinLogProcessorAgency;
import com.ooo.binlog.core.BinLogListener;
import com.ooo.binlog.model.Column;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 监听器启动器
 * @Author lzh
 * @Date 2:35 下午 19/8/2020
 * @Param
 * @return
 **/
@Slf4j
public class BinlogThreadStarter {

    private static Connection connection;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void runThread(MysqlHost conf,List<BinLogProcessorAgency> listenerData) {
        if(!conf.isEnable()) {
            log.warn("监听节点：{} 未开启的 binlog 连接信息", JSON.toJSONString(conf));
            return;
        }

        BinLogListener mysqlBinLogListener = new BinLogListener();
        listenerData.stream().forEach(listenerProperty ->{
            //设置binlog转换器
            Map<String, Column> columnMap = getColMap(listenerProperty.getDatabase(),listenerProperty.getTable(),conf);
            listenerProperty.setTableColumn(columnMap);

            try {
                mysqlBinLogListener.regListener(listenerProperty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        new Thread(new BinLogListenerThread(conf, mysqlBinLogListener)).start();
    }


    public Map<String, Column> getColMap(String db, String table, MysqlHost conf) {
        try {
            PreparedStatement ps
                    = getConnection(conf).prepareStatement("SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? and TABLE_NAME = ?");
            ps.setString(1, db);
            ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            Map<String, Column> map = new HashMap<>(rs.getRow());
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEMA");
                String tableName = rs.getString("TABLE_NAME");
                String column = rs.getString("COLUMN_NAME");
                int idx = rs.getInt("ORDINAL_POSITION");
                String dataType = rs.getString("DATA_TYPE");
                if (column != null && idx >= 1) {
                    // sql的位置从1开始
                    map.put(column, new Column(schema, tableName, idx - 1, column, dataType));
                }
            }
            ps.close();
            rs.close();
            return map;
        } catch (SQLException e) {
            log.error("load db conf error, db_table={}:{} ", db, table, e);
        }
        return null;
    }
    private Connection getConnection(MysqlHost conf){
        if(Objects.isNull(connection)){
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + conf.getHost() + ":" + conf.getPort(), conf.getUsername(), conf.getPassword());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return connection;
    }

}
