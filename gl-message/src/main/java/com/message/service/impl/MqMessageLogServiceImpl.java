package com.message.service.impl;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.constant.RabbitConstant;
import com.message.dao.MqMessageLogDao;
import com.message.entity.MqMessageLog;
import com.message.service.MqMessageLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class MqMessageLogServiceImpl extends ServiceImpl<MqMessageLogDao, MqMessageLog>
        implements MqMessageLogService {
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public void saveGoodsApplyTimeoutLog(String associatedId) {
        // 生成messageId
        String messageId = LocalDateTime.now()
                .format(DateTimeFormatter
                        .ofPattern("yyyyMMddHHmmss")) + snowflakeIdGenerator.nextId() % 10000;
        MqMessageLog mqMessageLog = new MqMessageLog();
        mqMessageLog.setMessageId(messageId);
        mqMessageLog.setBizId(associatedId);
        mqMessageLog.setExchange(RabbitConstant.GOODS_TTL_EXCHANGE);
        mqMessageLog.setRoutingKey(RabbitConstant.GOODS_TTL_ROUTING_KEY);
        mqMessageLog.setMessage("订单支付超时" + associatedId);
        mqMessageLog.setStatus((short) 1);
        saveLog(mqMessageLog);
    }

    @Override
    public void saveGoodsApplyTimeoutFailedLog(String associatedId, String message) {
        // 生成messageId
        String messageId = LocalDateTime.now()
                .format(DateTimeFormatter
                        .ofPattern("yyyyMMddHHmmss")) + snowflakeIdGenerator.nextId() % 10000;
        MqMessageLog mqMessageLog = new MqMessageLog();
        mqMessageLog.setMessageId(messageId);
        mqMessageLog.setBizId(associatedId);
        mqMessageLog.setExchange(RabbitConstant.GOODS_TTL_EXCHANGE);
        mqMessageLog.setRoutingKey(RabbitConstant.GOODS_TTL_ROUTING_KEY);
        mqMessageLog.setMessage("订单延时消费失败" + message);
        mqMessageLog.setStatus((short) 2);
        saveLog(mqMessageLog);
    }

    private void saveLog(MqMessageLog mqMessageLog) {
        save(mqMessageLog);
    }
}

