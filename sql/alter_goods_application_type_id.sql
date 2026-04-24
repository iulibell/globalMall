-- 上架申请关联商品类型（与 mall-admin 实体 PortalGoodsApplication.typeId 一致）
-- 若库表早于该字段创建，执行本脚本即可修复 Unknown column 'type_id' in 'field list'
-- 可重复执行：已存在 type_id 时不会报错

SET @db = DATABASE();
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'goods_application' AND COLUMN_NAME = 'type_id'
);
SET @sql := IF(
  @exists = 0,
  'ALTER TABLE `goods_application` ADD COLUMN `type_id` BIGINT NULL COMMENT ''商品类型id（portal_goods_type.type_id）'' AFTER `sku_name`',
  'SELECT ''goods_application.type_id already exists'' AS `notice`'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
