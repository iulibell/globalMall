package com.portal.service.client;

import com.common.api.CommonResult;
import com.portal.dto.OmsOrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("logi-oms")
public interface OmsServiceClient {
    @PostMapping("/oms/sys/addOrder")
    CommonResult<?> addOrder(@RequestBody OmsOrderDto omsOrderDto);

    @PostMapping("/oms/sys/payForOrder")
    CommonResult<?> payForOrder(@RequestParam String orderId);

    @PostMapping("/oms/sys/cancelOrder")
    CommonResult<?> cancelOrder(@RequestParam String orderId);

    @GetMapping("/oms/sys/getOrderByUser")
    CommonResult<?> getOrder(@RequestParam String userId, @RequestParam int pageNum, @RequestParam int pageSize);

    @GetMapping("/oms/sys/getOrderById")
    CommonResult<OmsOrderDto> getOrderById(@RequestParam String orderId);

    @GetMapping("/oms/sys/getOrderPayDeadline")
    CommonResult<?> getOrderPayDeadline(@RequestParam String orderId);
}
