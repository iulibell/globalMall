package com.portal.service.client;

import com.common.api.CommonResult;
import com.portal.dto.WmsOutboundCreateDto;
import com.portal.dto.WmsWarehouseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("logi-wms")
public interface WmsServiceClient {
    @PostMapping("/wms/payForInbound")
    CommonResult<?> payForInbound(@RequestParam String applyId);

    @PostMapping("/wms/outbound/create")
    CommonResult<?> createOutbound(@RequestBody WmsOutboundCreateDto wmsOutboundCreateDto);

    @GetMapping("/wms/getAvailableWarehouse")
    List<WmsWarehouseDto> getAvailableWarehouse(@RequestParam(defaultValue = "1")int pageNum,
                                                @RequestParam(defaultValue = "10")int pageSize);
}
