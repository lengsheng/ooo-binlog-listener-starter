package com.ooo.binlog.processor;


/**
 * 监听器
 * @Author lzh
 * @Date 2:33 下午 19/8/2020
 * @Param
 * @return
 **/
public interface BinLogProcessor {

    void onUpdate(String id, Object after, Object before);

    void onInsert(String id, Object data);

    void onDelete(String id, Object data);
}
