package com.ooo.binlog.thread;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.ooo.binlog.config.MysqlHost;
import com.ooo.binlog.core.BinLogListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 监听器 处理线程实现
 * @Author lzh
 * @Date 2:35 下午 19/8/2020
 * @Param
 * @return
 **/
@Slf4j
public class BinLogListenerThread implements Runnable{

    private MysqlHost host;

    private BinLogListener listener;

    public BinLogListenerThread(MysqlHost host, BinLogListener listener) {
        this.host = host;
        this.listener = listener;
    }

    @Override
    public void run() {
        BinaryLogClient client = new BinaryLogClient(host.getHost(), host.getPort(), host.getUsername(), host.getPassword());
        client.registerEventListener(listener);

        while (true) {
            try {
                client.connect();
                log.info("{}:{}监听器开启", host.getHost(), host.getPort());
            } catch (IOException e) {
                log.error("{}:{}监听器错误", host.getHost(), host.getPort(), e);
            }
        }
    }
}
