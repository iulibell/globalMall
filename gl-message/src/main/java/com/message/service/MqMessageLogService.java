package com.message.service;

public interface MqMessageLogService {
    /**
     * 记录订单支付超时日志
     * @param applyId 申请id
     */
    void saveGoodsApplyTimeoutLog(String applyId);

    /**
     * 记录订单死信队列消费失败日志
     * @param applyId 申请id
     * @param message 消息
     */
    void saveGoodsApplyTimeoutFailedLog(String applyId, String message);
}
