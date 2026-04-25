package com.portal.service;

import com.portal.dto.OmsCartDto;
import com.portal.dto.OmsCartSettlePreviewDto;

import java.util.List;

public interface OmsCartService {
    void addCart(OmsCartDto omsCartDto);

    void updateQuantity(Long id, Integer quantity);

    void checkCart(Long id, Short checked);

    void checkAll(Short checked);

    void deleteCart(Long id);

    List<OmsCartDto> listCart();

    OmsCartSettlePreviewDto settlePreview();

    void clearCheckedCart(String userId);

    void clearBoughtCart(String userId, String goodsId, String skuCode);
}
