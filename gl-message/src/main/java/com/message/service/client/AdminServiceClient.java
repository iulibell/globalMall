package com.message.service.client;

import com.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mall-admin")
public interface AdminServiceClient {
    @PostMapping("/admin/sys/markGoodsApplyPaymentTimeout")
    CommonResult<Boolean> markGoodsApplyPaymentTimeout(@RequestParam String applyId);
}
