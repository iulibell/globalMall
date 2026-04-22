package com.common.api;

/**
 * 与前端 dictionary.dict_type=api_message 的 dict_value 对齐，便于国际化展示。
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "operation_success"),
    FAIL(400, "operation_failed"),
    VALIDATION_ERROR(404, "validation_failed"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    INTERNAL_ERROR(500, "internal_error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}