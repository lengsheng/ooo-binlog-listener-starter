package com.ooo.binlog.core;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.ooo.binlog.model.MysqlTable;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.github.shyiko.mysql.binlog.event.EventType.isUpdate;

/**
 * binlog监听器注册，onEvent接收binlog消息
 * @Author lzh
 * @Date 2:33 下午 19/8/2020
 * @Param
 * @return
 **/
@Slf4j
public class BinLogListener implements BinaryLogClient.EventListener{

    //存放每张数据表对应的listener
    private Map<String, BinLogProcessorAgency> listeners;
    private Map<Long, MysqlTable> tableIdMap = new HashMap<>();


    public BinLogListener(){
        this.listeners = new HashMap<>();
    }

    public void regListener(BinLogProcessorAgency listenerAgency) throws Exception {
        //保存当前注册的listener
        listeners.put(listenerAgency.getListener().getDbTable(), listenerAgency);
        log.info("Ooo binlog listener [{} : {}] register succeed.",listenerAgency.getListener().getDatabase(),listenerAgency.getListener().getClass().getSimpleName());
    }

    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();

        if (eventType == EventType.TABLE_MAP) {
            MysqlTable table = new MysqlTable(event.getData());
            String key = table.getDatabase() + "." + table.getTable();
            if (this.listeners.containsKey(key))
                tableIdMap.put(table.getId(), table);
        }else if (isUpdate(eventType) ){
            UpdateRowsEventData data = event.getData();
            if (!tableIdMap.containsKey(data.getTableId()))
                return;
            dispatchEvent(data);
        }
//        else if (EventType.isUpdate(eventType)) {
//            UpdateRowsEventData data = event.getData();
//            if (!tableIdMap.containsKey(data.getTableId()))
//                return;
//
//            dispatchEvent(data);
//        } 
        else if (EventType.isWrite(eventType)) {
            WriteRowsEventData data = event.getData();
            if (!tableIdMap.containsKey(data.getTableId()))
                return;
            dispatchEvent(data);
        } else if (EventType.isDelete(eventType)) {
            DeleteRowsEventData data = event.getData();
            if (!tableIdMap.containsKey(data.getTableId()))
                return;

            dispatchEvent(data);
        }
    }

    private void dispatchEvent(DeleteRowsEventData data) {
        MysqlTable table = tableIdMap.get(data.getTableId());
        String key = getDbTable(table.getDatabase(),table.getTable());
        BinLogProcessorAgency listener = listeners.get(key);
        listener.invokeDelete(data.getRows());
    }

    private void dispatchEvent(WriteRowsEventData data) {
        MysqlTable table = tableIdMap.get(data.getTableId());
        String key = getDbTable(table.getDatabase(),table.getTable());
        BinLogProcessorAgency listener = listeners.get(key);
        listener.invokeInsert(data.getRows());
    }

    private void dispatchEvent(UpdateRowsEventData data) {
        MysqlTable table = tableIdMap.get(data.getTableId());
        String key = getDbTable(table.getDatabase(),table.getTable());
        BinLogProcessorAgency listener = listeners.get(key);
        if (listener == null) {
            return;
        }
        listener.invokeUpdate(data.getRows());
    }

    private static String getDbTable(String db, String table) {
        return db + "." + table;
    }

}
