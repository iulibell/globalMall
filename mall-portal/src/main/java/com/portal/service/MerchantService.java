package com.portal.service;

import com.common.api.CommonResult;
import com.portal.dto.*;

import java.util.List;

public interface MerchantService {
    /**
     * 商家在上架申请审核通过后支付入库费用，前台侧同步创建商品基础信息。
     * @param applyId 申请id
     */
    void payForInbound(String applyId);
    /**
     * 商家取消待支付的上架申请（将 mallStatus 置为 3）
     * @param applyId 申请id
     */
    void cancelGoodsApply(String applyId);
    /**
     * 商家申请上架商品(merchant操作)
     * @param portalGoodsApplicationDto 商品上架申请dto
     */
    void applyGoods(PortalGoodsApplicationDto portalGoodsApplicationDto);

    /**
     * 申请商品下架，需支付运费与下架处理费用(merchant操作)
     * @param goodsId 商品id
     */
    void applyForOffShelf(String goodsId);

    /**
     * 支付下架费用，支付成功后创建wms出库单
     *
     * @param portalOffShelfPayDto 下架支付参数
     */
    void payForOffShelf(PortalOffShelfPayDto portalOffShelfPayDto);

    /**
     * 下架申请支付超时：仅当申请仍为「未支付(0)」时置为「超时未支付(2)」。
     *
     * @param offShelfId 下架申请id
     * @return 本次是否更新成功
     */
    boolean markOffShelfPaymentTimeout(Long offShelfId);

    /**
     * 获取上架申请列表(merchant操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @param merchantId 商家id
     * @return 上架申请列表
     */
    CommonResult<?> getGoodsApplication(int pageNum, int pageSize, String merchantId);

    /**
     * 获取属于商家的商品(merchant操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @param merchantId 商家id
     * @return 商品列表
     */
    List<PortalGoodsDto> getPortalGoods(int pageNum, int pageSize, String merchantId);

    /**
     * 获取logi系统中可用的仓库供商家商品上架申请时选择
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 可用仓库列表
     */
    List<WmsWarehouseDto> getAvailableWarehouse(int pageNum, int pageSize);

    /**
     * 更新个人信息(merchant操作)
     *
     * @param sysUserInfoDto 用户信息dto
     */
    void updateInfo(SysUserInfoDto sysUserInfoDto);
}
