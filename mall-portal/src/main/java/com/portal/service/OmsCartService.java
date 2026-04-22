package com.portal.service;

import com.portal.dto.OmsCartDto;
import com.portal.dto.OmsCartSettlePreviewDto;

import java.util.List;

public interface OmsCartService {
    void addCart(OmsCartDto omsCartDto);

    void updateQuantity(Long id, String userId, Integer quantity);

    void checkCart(Long id, String userId, Short checked);

    void checkAll(String userId, Short checked);

    void deleteCart(Long id, String userId);

    List<OmsCartDto> listCart(String userId);

    OmsCartSettlePreviewDto settlePreview(String userId);

    void clearCheckedCart(String userId);

    void clearBoughtCart(String userId, String goodsId, String skuCode);
}
