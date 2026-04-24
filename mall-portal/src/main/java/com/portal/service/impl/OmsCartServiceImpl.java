package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.exception.Assert;
import com.portal.dao.OmsCartDao;
import com.portal.dto.OmsCartDto;
import com.portal.dto.OmsCartSettlePreviewDto;
import com.portal.entity.OmsCart;
import com.portal.service.OmsCartService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OmsCartServiceImpl implements OmsCartService {
    @Resource
    private OmsCartDao omsCartDao;

    @Override
    public void addCart(OmsCartDto omsCartDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (omsCartDto.getQuantity() == null || omsCartDto.getQuantity() <= 0) {
            Assert.fail("购买数量必须大于0");
        }
        Date now = new Date();
        LambdaQueryWrapper<OmsCart> wrapper = new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getUserId, omsCartDto.getUserId())
                .eq(OmsCart::getGoodsId, omsCartDto.getGoodsId())
                .eq(OmsCart::getDeleted, (short) 0);
        if (StrUtil.isEmpty(omsCartDto.getSkuCode())) {
            wrapper.isNull(OmsCart::getSkuCode);
        } else {
            wrapper.eq(OmsCart::getSkuCode, omsCartDto.getSkuCode());
        }
        OmsCart exist = omsCartDao.selectOne(wrapper);
        if (exist != null) {
            omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                    .eq(OmsCart::getId, exist.getId())
                    .setSql("quantity = quantity + {0}", omsCartDto.getQuantity())
                    .set(OmsCart::getUpdateTime, now));
            return;
        }
        OmsCart omsCart = new OmsCart();
        BeanUtils.copyProperties(omsCartDto, omsCart);
        omsCart.setChecked(omsCartDto.getChecked() == null ? (short) 1 : omsCartDto.getChecked());
        omsCart.setDeleted((short) 0);
        omsCart.setCreateTime(now);
        omsCart.setUpdateTime(now);
        omsCartDao.insert(omsCart);
    }

    @Override
    public void updateQuantity(Long id, String userId, Integer quantity) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (quantity == null || quantity <= 0) {
            Assert.fail("购买数量必须大于0");
        }
        int rows = omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getId, id)
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .set(OmsCart::getQuantity, quantity)
                .set(OmsCart::getUpdateTime, new Date()));
        if (rows == 0) {
            Assert.fail("购物车记录不存在");
        }
    }

    @Override
    public void checkCart(Long id, String userId, Short checked) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (checked == null || (checked != 0 && checked != 1)) {
            Assert.fail("勾选状态无效");
        }
        int rows = omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getId, id)
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .set(OmsCart::getChecked, checked)
                .set(OmsCart::getUpdateTime, new Date()));
        if (rows == 0) {
            Assert.fail("购物车记录不存在");
        }
    }

    @Override
    public void checkAll(String userId, Short checked) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (checked == null || (checked != 0 && checked != 1)) {
            Assert.fail("勾选状态无效");
        }
        omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .set(OmsCart::getChecked, checked)
                .set(OmsCart::getUpdateTime, new Date()));
    }

    @Override
    public void deleteCart(Long id, String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        int rows = omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getId, id)
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .set(OmsCart::getDeleted, (short) 1)
                .set(OmsCart::getUpdateTime, new Date()));
        if (rows == 0) {
            Assert.fail("购物车记录不存在");
        }
    }

    @Override
    public List<OmsCartDto> listCart(String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        List<OmsCart> list = omsCartDao.selectList(new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .orderByDesc(OmsCart::getUpdateTime));
        return list.stream().map(omsCart -> {
            OmsCartDto omsCartDto = new OmsCartDto();
            BeanUtils.copyProperties(omsCart, omsCartDto);
            return omsCartDto;
        }).toList();
    }

    @Override
    public OmsCartSettlePreviewDto settlePreview(String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        List<OmsCart> checkedList = omsCartDao.selectList(new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .eq(OmsCart::getChecked, (short) 1));
        OmsCartSettlePreviewDto previewDto = new OmsCartSettlePreviewDto();
        previewDto.setCheckedItemCount(checkedList.size());
        int quantityTotal = checkedList.stream()
                .map(OmsCart::getQuantity)
                .filter(quantity -> quantity != null && quantity > 0)
                .mapToInt(Integer::intValue)
                .sum();
        previewDto.setCheckedQuantityTotal(quantityTotal);
        BigDecimal totalAmount = checkedList.stream()
                .map(item -> {
                    if (item.getPrice() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                        return BigDecimal.ZERO;
                    }
                    return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        previewDto.setTotalAmount(totalAmount);
        return previewDto;
    }

    @Override
    public void clearCheckedCart(String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        omsCartDao.update(null, new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getDeleted, (short) 0)
                .eq(OmsCart::getChecked, (short) 1)
                .set(OmsCart::getDeleted, (short) 1)
                .set(OmsCart::getUpdateTime, new Date()));
    }

    @Override
    public void clearBoughtCart(String userId, String goodsId, String skuCode) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        LambdaUpdateWrapper<OmsCart> updateWrapper = new LambdaUpdateWrapper<OmsCart>()
                .eq(OmsCart::getUserId, userId)
                .eq(OmsCart::getGoodsId, goodsId)
                .eq(OmsCart::getDeleted, (short) 0)
                .set(OmsCart::getDeleted, (short) 1)
                .set(OmsCart::getUpdateTime, new Date());
        if (StrUtil.isEmpty(skuCode)) {
            updateWrapper.isNull(OmsCart::getSkuCode);
        } else {
            updateWrapper.eq(OmsCart::getSkuCode, skuCode);
        }
        omsCartDao.update(null, updateWrapper);
    }
}
