package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.PortalBrandDto;
import com.portal.service.PortalBrandService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal")
public class PortalBrandController {
    @Resource
    private PortalBrandService portalBrandService;

    @GetMapping("/manager/getBrand")
    public CommonResult<?> getBrand(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(portalBrandService.getBrand(pageNum, pageSize));
    }

    @PostMapping("/manager/addBrand")
    public CommonResult<?> addBrand(@Valid @RequestBody PortalBrandDto portalBrandDto) {
        portalBrandService.addBrand(portalBrandDto);
        return CommonResult.success("品牌新增成功");
    }

    @PostMapping("/manager/updateBrand")
    public CommonResult<?> updateBrand(@Valid @RequestBody PortalBrandDto portalBrandDto) {
        portalBrandService.updateBrand(portalBrandDto);
        return CommonResult.success("品牌更新成功");
    }

    @PostMapping("/manager/deleteBrand")
    public CommonResult<?> deleteBrand(@RequestParam Long id) {
        portalBrandService.deleteBrand(id);
        return CommonResult.success("品牌删除成功");
    }
}
