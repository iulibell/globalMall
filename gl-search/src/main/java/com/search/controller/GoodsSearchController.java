package com.search.controller;

import com.common.api.CommonResult;
import com.search.dto.GoodsIndexRequest;
import com.search.dto.GoodsSearchPageVo;
import com.search.service.GoodsSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search/goods")
@Tag(name = "GoodsSearch", description = "商品 ES 索引与检索")
public class GoodsSearchController {

    @Resource
    private GoodsSearchService goodsSearchService;

    @PostMapping("/addIndex")
    @Operation(summary = "写入或更新商品文档", description = "门户/WMS 在上架或信息变更后调用，同步到 ES。")
    public CommonResult<Void> addIndex(@Valid @RequestBody GoodsIndexRequest request) {
        goodsSearchService.indexGoods(request);
        return CommonResult.success(null);
    }

    @PostMapping("/deleteGoodsDocument")
    @Operation(summary = "按 goodsId 删除索引文档")
    public CommonResult<Void> deleteGoodsDocument(@RequestParam String goodsId) {
        goodsSearchService.removeGoods(goodsId);
        return CommonResult.success(null);
    }

    @GetMapping
    @Operation(summary = "关键词搜索商品", description = "query 为空时返回已上架商品分页；仅 status=1 参与检索。")
    public CommonResult<GoodsSearchPageVo> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return CommonResult.success(goodsSearchService.search(query, page, size));
    }
}
