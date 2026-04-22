package com.admin.service.client;

import com.admin.dto.PortalGoodsDto;
import com.admin.dto.PortalBrandDto;
import com.admin.dto.PortalGoodsTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("mall-portal")
public interface PortalServiceClient {
    @GetMapping("/portal/manager/getPortalGoodsByCategory")
    List<PortalGoodsDto> getRegularPortalGoods(@RequestParam int pageNum,
                                               @RequestParam int pageSize,
                                               @RequestParam Short category);

    @GetMapping("/portal/manager/getPortalGoodsById")
    PortalGoodsDto getPortalGoodsById(@RequestParam String goodsId);

    @GetMapping("/portal/manager/getGoodsType")
    List<PortalGoodsTypeDto> getGoodsType(@RequestParam int pageNum,
                                          @RequestParam int pageSize);

    @PostMapping("/portal/manager/addGoodsType")
    void addGoodsType(@RequestBody PortalGoodsTypeDto portalGoodsTypeDto);

    @PostMapping("/portal/manager/updateGoodsType")
    void updateGoodsType(@RequestBody PortalGoodsTypeDto portalGoodsTypeDto);

    @PostMapping("/portal/manager/deleteGoodsType")
    void deleteGoodsType(@RequestParam Long typeId);

    @GetMapping("/portal/manager/getBrand")
    List<PortalBrandDto> getBrand(@RequestParam int pageNum,
                                  @RequestParam int pageSize);

    @PostMapping("/portal/manager/addBrand")
    void addBrand(@RequestBody PortalBrandDto portalBrandDto);

    @PostMapping("/portal/manager/updateBrand")
    void updateBrand(@RequestBody PortalBrandDto portalBrandDto);

    @PostMapping("/portal/manager/deleteBrand")
    void deleteBrand(@RequestParam Long id);
}
