package com.admin.service.client;

import com.admin.dto.WmsInboundApplyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("logi-wms")
public interface WmsServiceClient {
    @PostMapping("/wms/addInboundApply")
    void addInboundApply(@RequestBody WmsInboundApplyDto wmsInboundApplyDto);
}
