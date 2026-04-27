package com.portal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.portal.entity.SeckillActivityGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SeckillActivityGoodsDao extends BaseMapper<SeckillActivityGoods> {

    /**
     * 原子扣减秒杀可用库存。
     * <p>
     * 通过 {@code available_stock >= qty} 条件避免并发场景下库存扣为负数，
     * 返回值为 0 代表库存不足或并发竞争失败。
     *
     * @param id  活动商品 ID
     * @param qty 扣减数量
     * @return 受影响行数
     */
    @Update("UPDATE seckill_activity_goods SET available_stock = available_stock - #{qty}, update_time = NOW(3) "
            + "WHERE id = #{id} AND available_stock >= #{qty}")
    int decreaseAvailableStock(@Param("id") Long id, @Param("qty") int qty);

    /**
     * 回补秒杀可用库存。
     *
     * @param id  活动商品 ID
     * @param qty 回补数量
     * @return 受影响行数
     */
    @Update("UPDATE seckill_activity_goods SET available_stock = available_stock + #{qty}, update_time = NOW(3) "
            + "WHERE id = #{id}")
    int increaseAvailableStock(@Param("id") Long id, @Param("qty") int qty);
}
