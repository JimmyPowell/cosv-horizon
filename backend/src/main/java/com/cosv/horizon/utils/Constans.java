package com.cosv.horizon.utils;

public class Constans {

    /**
     * 状态码
     */
    public static final String SUCCESS_CODE = "200"; // 成功状态码
    public static final String EXIST_CODE = "400"; // 失败状态码
    public static final String NO_AUTH_CODE = "401"; // 无权限状态码
    public static final String AUTH_ERROR_CODE = "403"; // 认证失败状态码，重新登录
    public static final String DUPLICATE_KEY_CODE = "409"; // 重复键状态码

    public static final String NO_USER_CODE = "1001"; // 没有查询到用户
    public static final String CODE_ERROR_CODE = "1002"; // 验证码过期
    public static final String CODE_ERROR_CODE2 = "1003";//验证码错误
    public static final String OSS_ERROR_CODE = "1004"; // 账号或密码错误
    public static final String TOKEN_EXPIRED_CODE = "1005"; // token过期


    /**
     * 消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String TOKEN_EXPIRED_MESSAGE = "token过期";
    public static final String OSS_ERROR_MEAASGE = "对象存储配置出错";
    public static final String EXIST_MESSAGE = "系统异常";
    public static final String TOKEN_ERROR = "身份验证失败，请重新登录";
    public static final String NO_USER = "没有查询到用户";
    public static final String PASSWORD_ERROR = "账号或密码错误";
    public static final String AUTH_FAILED = "认证失败";
    public static final String NO_AUTH = "没有访问权限";
    public static final String NETWORK_ERROR = "网络异常";
    public static final String DUPLICATED_NICKNAME = "昵称重复";
    public static final String DUPLICATE_KEY_MESSAGE = "版本号已存在，请使用其他版本号。";
}