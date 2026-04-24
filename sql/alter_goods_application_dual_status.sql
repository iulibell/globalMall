-- 商品上架申请状态拆分：status -> mall_status + logistic_status
-- 可重复执行；适用于已有 global_mall 库

-- 1) 新增 mall_status（若不存在）
SET @s := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'goods_application'
        AND COLUMN_NAME = 'mall_status'
    ),
    'SELECT 1',
    'ALTER TABLE `goods_application` ADD COLUMN `mall_status` SMALLINT NULL COMMENT ''商城审核状态:0待审核,1通过,2退回,3取消'' AFTER `description`'
  )
);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 新增 logistic_status（若不存在）
SET @s := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'goods_application'
        AND COLUMN_NAME = 'logistic_status'
    ),
    'SELECT 1',
    'ALTER TABLE `goods_application` ADD COLUMN `logistic_status` SMALLINT NULL COMMENT ''物流审核状态:0待审核,1通过,2退回'' AFTER `mall_status`'
  )
);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 迁移历史 status -> mall_status（仅 mall_status 为空时）
UPDATE `goods_application`
SET `mall_status` = `status`
WHERE `mall_status` IS NULL;

-- 4) logistic_status 缺省置 0（待审核）
UPDATE `goods_application`
SET `logistic_status` = 0
WHERE `logistic_status` IS NULL;

-- 5) 新增 apply_quantity（若不存在）
SET @s := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'goods_application'
        AND COLUMN_NAME = 'apply_quantity'
    ),
    'SELECT 1',
    'ALTER TABLE `goods_application` ADD COLUMN `apply_quantity` INT NULL COMMENT ''申请数量'' AFTER `logistic_status`'
  )
);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) 历史数据兜底数量（无数量时默认 1）
UPDATE `goods_application`
SET `apply_quantity` = 1
WHERE `apply_quantity` IS NULL OR `apply_quantity` < 1;

-- 7) 删除旧列 status（若存在）
SET @s := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'goods_application'
        AND COLUMN_NAME = 'status'
    ),
    'ALTER TABLE `goods_application` DROP COLUMN `status`',
    'SELECT 1'
  )
);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8) 新增 transport_order_id（若不存在）
SET @s := (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'goods_application'
        AND COLUMN_NAME = 'transport_order_id'
    ),
    'SELECT 1',
    'ALTER TABLE `goods_application` ADD COLUMN `transport_order_id` VARCHAR(255) NULL COMMENT ''关联运输单号'' AFTER `is_pay`'
  )
);
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
