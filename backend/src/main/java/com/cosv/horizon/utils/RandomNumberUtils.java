package com.cosv.horizon.utils;

import java.util.UUID;

public class RandomNumberUtils {

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 随机数字字符串的长度
     * @return 指定长度的随机数字字符串
     */
    public static String generateRandomNumber(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }

        // 从 UUID 中提取数字
        String uuid = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        while (uuid.length() < length) {
            // 如果提取的数字不足指定长度，追加一个新的 UUID
            uuid += UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        }

        // 截取指定长度的随机数字字符串
        return uuid.substring(0, length);
    }
}
