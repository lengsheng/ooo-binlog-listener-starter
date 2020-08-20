package com.ooo.binlog.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

/**
 * @Author: chenhao
 * @Description:
 * @Date: Created in 17:15 2020-08-12
 * @Modified By:
 */
@Slf4j
public class MD5Util {

    public static boolean equles(Object from, Object to) {
        if(from == null || to == null) {
            log.error("数据为空");
            return false;
        }
        String formMd5 = DigestUtils.md5DigestAsHex(JSON.toJSONString(from).getBytes());
        String toMd5 = DigestUtils.md5DigestAsHex(JSON.toJSONString(to).getBytes());

        if(formMd5.equals(toMd5)) {
            return true;
        }
        return false;
    }
}
