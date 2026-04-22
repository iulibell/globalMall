package com.common.exception;

import com.common.api.CommonResult;
import com.common.api.IErrorCode;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 须组件扫描到 {@code com.exception}（或等价注册）；{@code @AutoConfiguration} 单独注册时部分环境下 {@link ControllerAdvice} 不生效。
 */
@ControllerAdvice
public class GlobalException {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    /**
     * 处理方法参数校验异常
     * @param e
     * @return 处理结果
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult<?> handleException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if(bindingResult.hasErrors()){
            FieldError fieldError = bindingResult.getFieldError();
            if(fieldError != null){
                message = fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validationError(message);
    }

    /**
     * 处理绑定数据的类型异常
     * @param e
     * @return 处理结果
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public CommonResult<?> handleException(BindException e){
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if(bindingResult.hasErrors()){
            FieldError fieldError = bindingResult.getFieldError();
            if(fieldError != null){
                message = fieldError.getDefaultMessage();
            }
        }
        return CommonResult.validationError(message);
    }

    /**
     * 业务断言失败（如登录账号不存在、密码错误等），避免未处理导致 HTTP 500
     */
    @ResponseBody
    @ExceptionHandler(ApiException.class)
    public CommonResult<?> handleApiException(ApiException e) {
        IErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            return CommonResult.failed(errorCode);
        }
        String msg = e.getMessage();
        return CommonResult.failed(msg != null && !msg.isEmpty() ? msg : "generic_error");
    }

    /**
     * Redis 不可用时登录写缓存会失败，返回明确提示而非裸 500
     */
    @ResponseBody
    @ExceptionHandler(RedisConnectionFailureException.class)
    public CommonResult<?> handleRedisConnectionFailure(RedisConnectionFailureException e) {
        return CommonResult.failed("redis_unavailable");
    }

    @ResponseBody
    @ExceptionHandler(RedisSystemException.class)
    public CommonResult<?> handleRedisSystem(RedisSystemException e) {
        Throwable c = e.getCause();
        if (c instanceof RedisConnectionFailureException) {
            return CommonResult.failed("redis_unavailable");
        }
        return CommonResult.failed("redis_error");
    }

    /** MyBatis 未绑定 SQL、SQL 错误等，避免直接 500 */
    @ResponseBody
    @ExceptionHandler({ MyBatisSystemException.class, PersistenceException.class })
    public CommonResult<?> handleMyBatis(RuntimeException e) {
        log.error("MyBatis/Persistence 异常（已转为友好提示返回前端）", e);
        return CommonResult.failed("data_access_error");
    }

    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    public CommonResult<?> handleDataAccess(DataAccessException e) {
        log.error("数据库访问异常（已转为友好提示返回前端），根因: {}", e.getMostSpecificCause().getMessage(), e);
        return CommonResult.failed("db_access_error");
    }
}
