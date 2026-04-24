package com.portal.service.client;

import com.portal.dto.SysUserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("mall-gl-system")
public interface SystemServiceClient {
    @PostMapping("/system/updateInfo")
    void updateInfo(SysUserInfoDto sysUserInfoDto);
}
