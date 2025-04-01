package com.cosv.horizon.entity;

/**
 * 原始COSV文件状态枚举
 * 定义文件可能的处理状态
 */
public enum RawCosvFileStatus {
    /**
     * 已上传 - 文件刚上传，等待处理
     */
    UPLOADED,
    
    /**
     * 处理中 - 文件正在被处理
     */
    PROCESSING,
    
    /**
     * 已处理 - 文件处理完成
     */
    PROCESSED,
    
    /**
     * 处理失败 - 文件处理过程中出错
     */
    FAILED,
    
    /**
     * 已废弃 - 文件被标记为废弃
     */
    DISCARDED;
    
    /**
     * 检查给定的状态是否有效
     * @param status 状态字符串
     * @return 是否为有效状态
     */
    public static boolean isValid(String status) {
        try {
            valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 