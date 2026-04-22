package com.common.api;

import lombok.Data;

@Data
public class CommonResult <T> {
    private int code;
    private String message;
    private T data;

    private CommonResult(){
    }

    private CommonResult (int code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 与下游返回体一致（如 Feign 将错误 JSON 转为统一 {@link CommonResult}）
     */
    public static <T> CommonResult<T> of(int code, String message, T data) {
        return new CommonResult<>(code, message, data);
    }

    /**
     * 操作成功
     * @param data
     * @return 操作成功结果
     */
    public static <T> CommonResult<T> success(T data){
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功且自定义顶层 {@code message}（避免把提示语误放进 {@code data}，导致前端只认 {@code code===200} 时文案不一致）
     */
    public static <T> CommonResult<T> successMsg(String message, T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> CommonResult<T> successMsg(String message) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 操作失败
     * @return 操作失败结果
     */
    public static <T> CommonResult<T> fail(){
        return new CommonResult<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage(), null);
    }

    /**
     * 操作失败
     * @param <T>
     * @param message
     * @param data
     * @return
     */
    public static <T> CommonResult<T> failed(String message){
        return new CommonResult<>(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 失败返回结果，可被validationError等调用
     * @param errorCode 错误码
     * @return 失败结果
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 验证错误
     * @return 验证错误结果
     */
    public static <T> CommonResult<T> validationError(){
        return failed(ResultCode.VALIDATION_ERROR);
    }

    /**
     * 验证错误
     * @param message 错误信息
     * @return 验证错误结果
     */
    public static <T> CommonResult<T> validationError(String message){
        return new CommonResult<>(ResultCode.VALIDATION_ERROR.getCode(), message, null);
    }

    /**
     * 未授权
     * @return 未授权结果
     */
    public static <T> CommonResult<T> unauthorized(){
        return failed(ResultCode.UNAUTHORIZED);
    }

    /**
     * 未授权
     * @param message 错误信息
     * @return 未授权结果
     */
    public static <T> CommonResult<T> unauthorized(T data){
        return new CommonResult<>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 禁止访问
     * @param message 错误信息
     * @return 禁止访问结果
     */
    public static <T> CommonResult<T> forbidden(){
        return failed(ResultCode.FORBIDDEN);
    }
}
