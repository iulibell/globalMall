package com.admin.controller;

import com.admin.dto.DictionaryDto;
import com.admin.dto.DictionaryOperationDto;
import com.admin.service.DictionaryService;
import com.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/sys")
@Tag(name = "DictController", description = "多语言与前端文案字典：公开按类型拉取；超管分页维护及增删改。")
public class DictionaryController {
    @Resource
    DictionaryService dictionaryService;

    @GetMapping("/getDictionary")
    @Operation(
            operationId = "dictGetByType",
            summary = "按类型获取字典（公开）",
            description = "门户/前端按 dictType 拉取当前语言下的字典项列表，无需超管权限。")
    public CommonResult<?> getDictionary(@RequestParam String dictType) {
        return CommonResult.success(dictionaryService.getDictionaryList(dictType));
    }

    @PostMapping("/super/updateDictionary")
    @Operation(summary = "更新字典项", description = "超管权限；修改指定字典键的多语言文案。")
    public CommonResult<?> updateDictionary(@RequestBody DictionaryOperationDto dictionaryOperationDto) {
        dictionaryService.updateDictionary(dictionaryOperationDto);
        return CommonResult.success("dict_updated");
    }

    @PostMapping("/super/addDictionary")
    @Operation(summary = "批量新增字典", description = "超管权限；一次提交多条字典行（多语言）。")
    public CommonResult<?> addDictionary(@RequestBody List<DictionaryDto> dictionaryDtoList) {
        dictionaryService.addDictionary(dictionaryDtoList);
        return CommonResult.success("dict_added");
    }

    @PostMapping("/super/deleteDictionary")
    @Operation(summary = "删除字典项", description = "超管权限；按操作 DTO 删除指定字典条目。")
    public CommonResult<?> deleteDictionary(@RequestBody DictionaryOperationDto dictionaryOperationDto) {
        dictionaryService.deleteDictionary(dictionaryOperationDto);
        return CommonResult.success("dict_deleted");
    }

    @GetMapping("/super/getDictionary")
    @Operation(
            operationId = "dictGetPage",
            summary = "分页浏览全部字典",
            description = "超管权限；分页查看字典表，用于后台维护。")
    public CommonResult<?> getDictionary(@RequestParam(defaultValue = "1")int pageNum,
                                         @RequestParam(defaultValue = "10")int pageSize){
        return CommonResult.success(dictionaryService.getDictionary(pageNum,pageSize));
    }
}
