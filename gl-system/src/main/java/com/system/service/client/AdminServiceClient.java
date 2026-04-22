package com.system.service.client;

import com.common.dto.RegisterParamDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-admin")
public interface AdminServiceClient {
    @PostMapping("/admin/sys/getRegisterFromSys")
    void applyRegister(@RequestBody RegisterParamDto registerParamDto);
}
