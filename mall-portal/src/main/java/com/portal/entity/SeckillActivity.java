package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "活动编码")
    private String activityCode;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "活动状态:0预约(super已保存,到开始时间后自动开启),1待开始(兼容旧数据),2进行中,3已结束,4已取消")
    private Short status;

    @Schema(description = "发起人(super)ID")
    private String launcherId;

    @Schema(description = "备注")
    private String remark;

    private Date createTime;
    private Date updateTime;
}
