package com.portal.service;

import com.portal.dto.OmsCartDto;
import com.portal.dto.OmsCartSettlePreviewDto;

import java.util.List;

/**
 * 门户购物车服务接口。
 * <p>
 * 负责购物车增删改查、勾选状态维护以及结算前预览能力。
 */
public interface OmsCartService {
    /**
     * 新增购物车项。
     *
     * @param omsCartDto 购物车参数
     */
    void addCart(OmsCartDto omsCartDto);

    /**
     * 修改购物车项购买数量。
     *
     * @param id       购物车项 ID
     * @param quantity 购买数量
     */
    void updateQuantity(Long id, Integer quantity);

    /**
     * 修改单条购物车项勾选状态。
     *
     * @param id      购物车项 ID
     * @param checked 勾选状态
     */
    void checkCart(Long id, Short checked);

    /**
     * 批量修改当前用户购物车勾选状态。
     *
     * @param checked 勾选状态
     */
    void checkAll(Short checked);

    /**
     * 删除指定购物车项。
     *
     * @param id 购物车项 ID
     */
    void deleteCart(Long id);

    /**
     * 查询当前用户购物车列表。
     *
     * @return 购物车列表
     */
    List<OmsCartDto> listCart();

    /**
     * 生成购物车结算预览信息（金额、项列表等）。
     *
     * @return 结算预览
     */
    OmsCartSettlePreviewDto settlePreview();

    /**
     * 清理用户所有已勾选购物车项。
     *
     * @param userId 用户 ID
     */
    void clearCheckedCart(String userId);

    /**
     * 按商品与规格清理用户已购买购物车项。
     *
     * @param userId  用户 ID
     * @param goodsId 商品 ID
     * @param skuCode SKU 编码
     */
    void clearBoughtCart(String userId, String goodsId, String skuCode);
}
