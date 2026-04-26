SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_code` VARCHAR(64) NOT NULL COMMENT '活动编码',
  `activity_name` VARCHAR(128) NOT NULL COMMENT '活动名称(super发起)',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '活动状态:0预约,1待开始(兼容),2进行中,3已结束,4已取消',
  `launcher_id` VARCHAR(255) NULL COMMENT '发起人(super)ID',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seckill_activity_code` (`activity_code`),
  KEY `idx_seckill_status_time` (`status`, `start_time`, `end_time`),
  KEY `idx_seckill_name_time` (`activity_name`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动主表（super发起）';

CREATE TABLE IF NOT EXISTS `seckill_activity_goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_code` VARCHAR(64) NOT NULL COMMENT '商品报名编码',
  `launch_activity_code` VARCHAR(64) NOT NULL COMMENT 'super发起活动编码',
  `activity_name` VARCHAR(128) NOT NULL COMMENT '活动名称快照',
  `goods_id` VARCHAR(255) NOT NULL COMMENT '关联商品id',
  `merchant_id` VARCHAR(255) NOT NULL COMMENT '报名商家id',
  `origin_price` DECIMAL(10,2) NOT NULL COMMENT '原价快照',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
  `total_stock` INT NOT NULL COMMENT '总库存',
  `available_stock` INT NOT NULL COMMENT '可用库存',
  `per_user_limit` INT NOT NULL DEFAULT 1 COMMENT '单用户限购数量',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态:1待审核,2未过审,3已过审',
  `reviewer_id` VARCHAR(255) NULL COMMENT '审核员id',
  `review_remark` VARCHAR(500) NULL COMMENT '审核备注',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seckill_activity_goods_code` (`activity_code`),
  KEY `idx_seckill_goods_status` (`status`, `goods_id`),
  KEY `idx_seckill_goods_merchant` (`merchant_id`),
  KEY `idx_seckill_goods_launch` (`launch_activity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动商品报名表';

