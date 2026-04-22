package com.message.config;

import com.common.constant.RabbitConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class RabbitConfig {
    @SuppressWarnings("null")
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /*=========== 商品上架申请订单锁延时队列 ============ */

    @Bean
    public Queue goodsTtlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", RabbitConstant.GOODS_TTL_EXPIRE);
        args.put("x-dead-letter-exchange", RabbitConstant.GOODS_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", RabbitConstant.GOODS_DEAD_ROUTING_KEY);
        return new Queue(
                RabbitConstant.GOODS_TTL_QUEUE,
                true,
                false,
                true,
                args
        );
    }

    @Bean
    public Exchange goodsTtlExchange() {
        return new DirectExchange(RabbitConstant.GOODS_TTL_EXCHANGE,true,false);
    }

    @Bean
    public Binding goodsTtlBinding() {
        return BindingBuilder
                .bind(goodsTtlQueue())
                .to(goodsTtlExchange())
                .with(RabbitConstant.GOODS_TTL_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue goodsDeadQueue() {
        return new Queue(
                RabbitConstant.GOODS_DEAD_QUEUE,
                true,
                false,
                false,
                null
        );
    }

    @Bean
    public Exchange orderDeadExchange() {
        return new DirectExchange(RabbitConstant.GOODS_DEAD_EXCHANGE);
    }

    @Bean
    public Binding goodsDeadBinding() {
        return BindingBuilder
                .bind(goodsDeadQueue())
                .to(orderDeadExchange())
                .with(RabbitConstant.GOODS_DEAD_ROUTING_KEY)
                .noargs();
    }
}
