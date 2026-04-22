package com.common.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.common.entity.WebLog;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import net.logstash.logback.marker.Markers;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;

@Component
@Aspect
public class WebLogAspect {
    private final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("execution(* com.*.controller.*.*(..)) || execution(* com.*.service.*.*(..))")
    public void webLogPointcut() {
    }

    @SuppressWarnings("null")
    @Around("webLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        WebLog webLog = new WebLog();
        Object result = null;
        String requestId = UUID.randomUUID().toString();

        try {
            // 设置请求ID到MDC
            MDC.put("requestId", requestId);
            webLog.setRequestId(requestId);
            result = joinPoint.proceed();
        } catch (Throwable e) {
            webLog.setException(e.getMessage());
            throw e;
        } finally {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            if(method.isAnnotationPresent(Operation.class)){
                Operation log = method.getAnnotation(Operation.class);
                webLog.setDescription(log.summary());
            }
            long endTime = System.currentTimeMillis();
            String urlStr = request.getRequestURL().toString();
            webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
            webLog.setUsername(request.getUserPrincipal().getName());
            webLog.setIp(request.getRemoteAddr()); // 修复IP获取
            webLog.setMethod(request.getMethod());
            webLog.setParameter(getParameter(method, joinPoint.getArgs()));
            webLog.setResult(result);
            webLog.setSpendTime((int) (endTime - startTime));
            webLog.setStartTime(startTime);
            webLog.setUri(request.getRequestURI());
            webLog.setUrl(request.getRequestURL().toString());

            Map<String, Object> logMap = new HashMap<>();
            logMap.put("url", webLog.getUrl());
            logMap.put("method", webLog.getMethod());
            logMap.put("parameter", webLog.getParameter());
            logMap.put("spendTime", webLog.getSpendTime());
            logMap.put("description", webLog.getDescription());
            logMap.put("requestId", requestId);

            logger.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());

            // 清理MDC
            MDC.remove("requestId");
        }
        return result;
    }

    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StringUtils.hasLength(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.isEmpty()) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
}

