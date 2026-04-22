package com.message.consumer;

import com.common.api.CommonResult;
import com.common.api.ResultCode;
import com.common.constant.RabbitConstant;
import com.common.exception.Assert;
import com.message.service.MqMessageLogService;
import com.message.service.client.AdminServiceClient;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class GoodsApplyConsumer {
    @Resource
    private AdminServiceClient adminServiceClient;
    @Resource
    private MqMessageLogService mqMessageLogService;

    @RabbitListener(queues = RabbitConstant.GOODS_DEAD_QUEUE)
    public void goodsDeadQueue(Channel channel, Message mqMessage, String applyId) throws IOException {
        if(applyId == null)
            Assert.fail("商品死信队列未接收到applyId");
        long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
        try{
            CommonResult<Boolean> result = adminServiceClient.markGoodsApplyPaymentTimeout(applyId);
            if (result != null
                    && result.getCode() == ResultCode.SUCCESS.getCode()
                    && Boolean.TRUE.equals(result.getData())) {
                mqMessageLogService.saveGoodsApplyTimeoutLog(applyId);
            }
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            log.info("订单死信队列出现异常" + e.getMessage());
            mqMessageLogService.saveGoodsApplyTimeoutFailedLog(applyId, e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
