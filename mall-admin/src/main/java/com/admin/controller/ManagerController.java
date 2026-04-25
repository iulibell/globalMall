package com.admin.controller;

import com.admin.dto.PortalGoodsTypeDto;
import com.admin.dto.PortalBrandDto;
import com.admin.service.ManagerService;
import com.common.api.CommonResult;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/manager")
public class ManagerController {
    @Resource
    private ManagerService managerService;

    @GetMapping("/getPortalGoods")
    public CommonResult<?> getPortalGoods(@RequestParam(defaultValue = "1")int pageNum,
                                          @RequestParam(defaultValue = "10")int pageSize,
                                          @RequestParam Short category){
        return CommonResult.success(managerService.getPortalGoods(pageNum,pageSize,category));
    }

    @GetMapping("/getPortalGoodsById")
    public CommonResult<?> getPortalGoodsById(@RequestParam String goodsId){
        return CommonResult.success(managerService.getPortalGoodsById(goodsId));
    }

    @GetMapping("/getGoodsType")
    public CommonResult<?> getGoodsType(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize){
        return CommonResult.success(managerService.getGoodsType(pageNum, pageSize));
    }

    @PostMapping("/addGoodsType")
    public CommonResult<?> addGoodsType(@Valid @RequestBody PortalGoodsTypeDto portalGoodsTypeDto){
        managerService.addGoodsType(portalGoodsTypeDto);
        return CommonResult.success("类型新增成功");
    }

    @PostMapping("/updateGoodsType")
    public CommonResult<?> updateGoodsType(@Valid @RequestBody PortalGoodsTypeDto portalGoodsTypeDto){
        managerService.updateGoodsType(portalGoodsTypeDto);
        return CommonResult.success("类型更新成功");
    }

    @PostMapping("/deleteGoodsType")
    public CommonResult<?> deleteGoodsType(@RequestParam Long typeId){
        managerService.deleteGoodsType(typeId);
        return CommonResult.success("类型删除成功");
    }

    @GetMapping("/getBrand")
    public CommonResult<?> getBrand(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize){
        return CommonResult.success(managerService.getBrand(pageNum, pageSize));
    }

    @PostMapping("/addBrand")
    public CommonResult<?> addBrand(@Valid @RequestBody PortalBrandDto portalBrandDto){
        managerService.addBrand(portalBrandDto);
        return CommonResult.success("品牌新增成功");
    }

    @PostMapping("/updateBrand")
    public CommonResult<?> updateBrand(@Valid @RequestBody PortalBrandDto portalBrandDto){
        managerService.updateBrand(portalBrandDto);
        return CommonResult.success("品牌更新成功");
    }

    @PostMapping("/deleteBrand")
    public CommonResult<?> deleteBrand(@RequestParam Long id){
        managerService.deleteBrand(id);
        return CommonResult.success("品牌删除成功");
    }
}
