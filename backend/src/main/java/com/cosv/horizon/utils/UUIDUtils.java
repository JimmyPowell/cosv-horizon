package com.cosv.horizon.utils;

import java.util.UUID;

public class UUIDUtils {

    /**
     * 生成一个标准格式的 UUID，例如：550e8400-e29b-41d4-a716-446655440000
     *
     * @return 标准格式的 UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成一个无分隔符的 UUID，例如：550e8400e29b41d4a716446655440000
     *
     * @return 无分隔符的 UUID 字符串
     */
    public static String generateUUIDWithoutHyphens() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}