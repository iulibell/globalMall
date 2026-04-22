package com.common.constant;

public class AuthConstant {
    // 存储于Session中的管理员信息
    public static final String STP_ADMIN_INFO = "adminInfo";
    /** 返回给前端的 Authorization 前缀，与网关/各服务读取请求头一致 */
    public static final String TOKEN_HEAD = "Bearer";
}
