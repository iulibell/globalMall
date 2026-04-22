package com.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
@TableName("mq_message_log")
@Schema(description = "消息队列日志实体类")
public class MqMessageLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 64)
    @Schema(description = "消息id")
    private String messageId;
    @Size(max = 64)
    @Schema(description = "关联id")
    private String bizId;
    @Size(max = 255)
    @Schema(description = "交换机名称")
    private String exchange;
    @Size(max = 255)
    @Schema(description = "路由键")
    private String routingKey;
    @Size(max = 500)
    @Schema(description = "具体信息")
    private String message;
    @Schema(description = "状态:1->消费成功(定时器清除),2->消费失败")
    private Short status;
    private Date createTime;
    private Date updateTime;
}
