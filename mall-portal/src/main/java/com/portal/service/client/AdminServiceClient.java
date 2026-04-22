package com.portal.service.client;

import com.common.api.CommonResult;
import com.portal.dto.PortalGoodsApplicationDto;
import com.portal.dto.PortalGoodsDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mall-admin")
public interface AdminServiceClient {
    @PostMapping("/admin/sys/addGoodsApplication")
    void addGoodsApplication(@Valid @RequestBody PortalGoodsApplicationDto portalGoodsApplicationDto);
    @GetMapping("/admin/merchant/getGoodsApplication")
    CommonResult<?> getGoodsApplication(@RequestParam(defaultValue = "1")int pageNum,
                                        @RequestParam(defaultValue = "10")int pageSize,
                                        @RequestParam String merchantId);
    @GetMapping("/admin/sys/getPortalDtoById")
    PortalGoodsDto getPortalDtoById(@RequestParam String applyId);
}
