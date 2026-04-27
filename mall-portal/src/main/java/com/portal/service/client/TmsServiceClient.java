package com.portal.service.client;

import com.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("logi-tms")
public interface TmsServiceClient {
    @PostMapping("/tms/sys/consigneeSign")
    CommonResult<Boolean> consigneeSign(@RequestParam String transportOrderId);
}

