package com.admin.service;

import com.admin.dto.PortalGoodsApplicationDto;
import com.admin.dto.PortalGoodsDto;
import com.admin.entity.RegisterApplication;
import com.common.dto.RegisterParamDto;

import java.math.BigDecimal;
import java.util.List;

public interface ReviewerService {
    /**
     * 从system模块中获取注册dto后插入注册审核表
     * @param registerParamDto 注册dto
     */
    void getRegisterFromSys(RegisterParamDto registerParamDto);

    /**
     * portal模块商家申请上架调用
     * @param portalGoodsApplicationDto 商品申请dto
     */
    void addGoodsApplication(PortalGoodsApplicationDto portalGoodsApplicationDto);

    /**
     * 通过注册申请审核(reviewer操作)
     * @param registerParamDto 注册dto
     */
    void accessRegister(RegisterParamDto registerParamDto);

    /**
     * 获取全部注册申请,定时任务进行清理已审核的申请(reviewer操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 注册申请列表
     */
    List<RegisterApplication> fetchRegisterApplication(int pageNum, int pageSize);

    /**
     * 退回注册申请审核(reviewer操作)
     * @param registerParamDto 注册dto
     */
    void rejectRegister(RegisterParamDto registerParamDto);

    /**
     * 获取商品上架申请列表
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 商品申请上架列表
     */
    List<PortalGoodsApplicationDto> getGoodsApplication(int pageNum, int pageSize);

    /**
     * 商品上架申请通过审核(reviewer操作)
     * @param applyId 申请id
     */
    void accessGoodsApply(String applyId);

    /**
     * 物流审核员通过商品的入库申请
     * @param applyId 申请id
     */
    void accessFromWms(String applyId, BigDecimal fee);

    /**
     * 商品上架申请退回(reviewer操作)
     * @param applyId 申请id
     * @param remark 退回理由
     */
    void rejectGoodsApply(String applyId, String remark);

    /**
     * 物流审核员退回商品的入库申请
     * @param applyId 申请id
     */
    void rejectFromWms(String applyId);

    /**
     * 商家获取自己的商品上架申请列表(merchant操作)
     * @param merchantId 商家id
     * @return 上架申请列表
     */
    List<PortalGoodsApplicationDto> getGoodsApplication(int pageNum, int pageSize, String merchantId);

    /**
     * 商家取消上架申请（仅待支付订单可取消）
     * @param applyId 申请id
     * @param merchantId 商家id
     */
    void cancelGoodsApplyByMerchant(String applyId, String merchantId);

    /**
     * 商家支付成功后绑定运输单号
     * @param applyId 上架申请id
     * @param merchantId 商家id
     * @param transportOrderId 运输单号
     */
    void bindTransportOrderId(String applyId, String merchantId, String transportOrderId);

    /**
     * 支付超时：仅当订单状态仍为「已过审(1)」的是否支付时置为「超时未支付(2)」。已支付、已超时、已取消/已删单则不修改。
     *
     * @param applyId 申请订单id
     * @return 本次是否将状态更新为 4
     */
    boolean markGoodsApplyPaymentTimeout(String applyId);

    /**
     * 回调方法
     * @param applyId 申请id
     * @return 商品上架申请dto
     */
    PortalGoodsDto getPortalDtoById(String applyId);
}
