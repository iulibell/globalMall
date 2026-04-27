package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.PortalGoodsTypeDto;
import com.portal.service.PortalGoodsTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "PortalGoodsTypeController", description = "门户商品类型接口：类型查询与管理维护")
@RestController
@RequestMapping("/portal")
public class PortalGoodsTypeController {
    @Resource
    private PortalGoodsTypeService portalGoodsTypeService;

    @GetMapping("/getGoodsType")
    public CommonResult<?> getGoodsType(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(portalGoodsTypeService.getType(pageNum, pageSize));
    }

    @PostMapping("/manager/addGoodsType")
    public CommonResult<?> addGoodsType(@Valid @RequestBody PortalGoodsTypeDto portalGoodsTypeDto) {
        portalGoodsTypeService.addType(portalGoodsTypeDto);
        return CommonResult.success("类型新增成功");
    }

    @PostMapping("/manager/updateGoodsType")
    public CommonResult<?> updateGoodsType(@Valid @RequestBody PortalGoodsTypeDto portalGoodsTypeDto) {
        portalGoodsTypeService.updateType(portalGoodsTypeDto);
        return CommonResult.success("类型更新成功");
    }

    @PostMapping("/manager/deleteGoodsType")
    public CommonResult<?> deleteGoodsType(@RequestParam Long typeId) {
        portalGoodsTypeService.deleteType(typeId);
        return CommonResult.success("类型删除成功");
    }
}
