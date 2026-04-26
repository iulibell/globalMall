-- globalMall 实体对应建表语句（MySQL）
-- 时间字段：create_time / update_time 与实体及 MyBatis-Plus 驼峰映射一致

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for oms_cart
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart`;
CREATE TABLE `oms_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL COMMENT '用户id',
  `merchant_id` varchar(255) DEFAULT NULL COMMENT '商家id',
  `goods_id` varchar(255) DEFAULT NULL COMMENT '商品id(对应wms库存的stockId)',
  `sku_code` varchar(50) DEFAULT NULL COMMENT '商品库存编号',
  `sku_name` varchar(50) DEFAULT NULL COMMENT '商品名称',
  `picture` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `quantity` int DEFAULT NULL COMMENT '购买数量',
  `checked` smallint DEFAULT NULL COMMENT '是否勾选:0->未勾选,1->已勾选',
  `deleted` smallint DEFAULT NULL COMMENT '删除标记:0->正常,1->已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

-- ----------------------------
-- Table structure for portal_goods_type
-- ----------------------------
DROP TABLE IF EXISTS `portal_goods_type`;
CREATE TABLE `portal_goods_type` (
  `type_id` bigint NOT NULL AUTO_INCREMENT,
  `type_name` varchar(20) DEFAULT NULL COMMENT '类型名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品类型';

-- ----------------------------
-- Table structure for portal_goods_off_shelf
-- ----------------------------
DROP TABLE IF EXISTS `portal_goods_off_shelf`;
CREATE TABLE `portal_goods_off_shelf` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` varchar(255) DEFAULT NULL COMMENT '商品id',
  `merchant_id` varchar(255) DEFAULT NULL COMMENT '商家id',
  `city` varchar(20) DEFAULT NULL COMMENT '商家所属城市',
  `fee` decimal(10,2) DEFAULT NULL COMMENT '所支付的下架处理费用与运费',
  `transport_order_id` varchar(255) DEFAULT NULL COMMENT '关联物流运输单号',
  `status` smallint DEFAULT NULL COMMENT '状态:0->待审核,1->待支付,2->已支付,3->超时未支付,4->下架完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品下架';

-- ----------------------------
-- Table structure for goods_application
-- ----------------------------
DROP TABLE IF EXISTS `goods_application`;
CREATE TABLE `goods_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_id` varchar(255) DEFAULT NULL COMMENT '商品上架申请id',
  `merchant_id` varchar(255) DEFAULT NULL COMMENT '商家id',
  `merchant_phone` varchar(11) DEFAULT NULL COMMENT '商家手机号',
  `sku_name` varchar(50) DEFAULT NULL,
  `type_id` bigint DEFAULT NULL COMMENT '商品类型id（portal_goods_type.type_id）',
  `price` decimal(10,2) DEFAULT NULL,
  `picture` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `mall_status` smallint DEFAULT NULL COMMENT '商城审核状态:0->待审核(默认),1->已通过,2->已退回,3->已取消',
  `logistic_status` smallint DEFAULT NULL COMMENT '物流审核状态:0->待审核(默认),1->已通过,2->已退回',
  `apply_quantity` int DEFAULT NULL COMMENT '申请数量',
  `city` varchar(20) DEFAULT NULL COMMENT '商家所在城市',
  `warehouse_city` varchar(20) DEFAULT NULL COMMENT '仓库所属城市',
  `is_pay` smallint DEFAULT NULL COMMENT '关联入库申请单是否已支付:0->未支付(默认),1->已支付,2->超时未支付',
  `transport_order_id` varchar(255) DEFAULT NULL COMMENT '关联运输单号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注(退回商品上架申请原因)',
  `fee` decimal(10,2) DEFAULT NULL COMMENT '入库申请支付费用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品上架申请';

-- ----------------------------
-- Table structure for portal_brand
-- ----------------------------
DROP TABLE IF EXISTS `portal_brand`;
CREATE TABLE `portal_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand_id` varchar(255) DEFAULT NULL COMMENT '品牌id',
  `brand_name` varchar(20) DEFAULT NULL COMMENT '品牌名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌';

-- ----------------------------
-- Table structure for dictionary
-- （实体无 create_time / update_time）
-- ----------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(50) DEFAULT NULL COMMENT '字典类型',
  `dict_name` varchar(500) DEFAULT NULL COMMENT '字典名称',
  `dict_value` varchar(50) DEFAULT NULL COMMENT '字典值',
  `sort` int DEFAULT NULL COMMENT '排序',
  `status` smallint DEFAULT NULL COMMENT '状态:0->禁用,1->启用(默认)',
  `lang` varchar(10) DEFAULT NULL COMMENT '语言: 1->cn中文,2->en英文,3->ru俄文',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典';

-- ----------------------------
-- Table structure for portal_goods
-- ----------------------------
DROP TABLE IF EXISTS `portal_goods`;
CREATE TABLE `portal_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` varchar(255) DEFAULT NULL COMMENT '商家id',
  `goods_id` varchar(255) DEFAULT NULL COMMENT '商品id(对应wms库存的stockId)',
  `warehouse_id` bigint DEFAULT NULL COMMENT '库存所属的仓库id',
  `location_id` bigint DEFAULT NULL COMMENT '库位id',
  `sku_name` varchar(50) DEFAULT NULL COMMENT '商品名称',
  `sku_code` varchar(50) DEFAULT NULL COMMENT '商品库存编号',
  `picture` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `category` smallint DEFAULT NULL COMMENT '商品种类:0->普通,1->特殊',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `type` varchar(20) DEFAULT NULL COMMENT '商品类型',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `visit_volume` bigint DEFAULT NULL COMMENT '访问次数(初始值为0)',
  `status` smallint DEFAULT NULL COMMENT '商品状态:0->未上架,1->已上架(可展示),2->已下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门户商品';

-- ----------------------------
-- Table structure for mq_message_log
-- ----------------------------
DROP TABLE IF EXISTS `mq_message_log`;
CREATE TABLE `mq_message_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` varchar(64) DEFAULT NULL COMMENT '消息id',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '关联id',
  `exchange` varchar(255) DEFAULT NULL COMMENT '交换机名称',
  `routing_key` varchar(255) DEFAULT NULL COMMENT '路由键',
  `message` varchar(500) DEFAULT NULL COMMENT '具体信息',
  `status` smallint DEFAULT NULL COMMENT '状态:1->消费成功(定时器清除),2->消费失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息队列日志';

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) DEFAULT NULL COMMENT '订单id(在此系统生成，联调物流系统)',
  `merchant_id` varchar(255) DEFAULT NULL COMMENT '商家id',
  `user_id` varchar(255) DEFAULT NULL COMMENT '用户id',
  `goods_id` varchar(255) DEFAULT NULL COMMENT '商品id(对应wms库存的stockId)',
  `warehouse_id` bigint DEFAULT NULL COMMENT '库存所属的仓库id',
  `location_id` bigint DEFAULT NULL COMMENT '库位id',
  `sku_name` varchar(50) DEFAULT NULL COMMENT '商品名称',
  `sku_code` varchar(50) DEFAULT NULL COMMENT '商品库存编号',
  `price` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `user_phone` varchar(11) DEFAULT NULL COMMENT '用户手机号',
  `merchant_phone` varchar(11) DEFAULT NULL COMMENT '商家手机号',
  `warehouse_city` varchar(30) DEFAULT NULL COMMENT '仓库所属城市',
  `city` varchar(30) DEFAULT NULL COMMENT '用户的城市',
  `category` smallint DEFAULT NULL COMMENT '商品种类:0->普通,1->特殊',
  `type` varchar(20) DEFAULT NULL COMMENT '商品类型',
  `status` smallint DEFAULT NULL COMMENT '订单状态: 0->待审核,1->未通过审核,2->已审核,3->待支付,4->超时未支付,5->已支付',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注(特殊商品未通过审核的原因)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单';

-- ----------------------------
-- Table structure for register_application
-- （实体无 create_time / update_time）
-- ----------------------------
DROP TABLE IF EXISTS `register_application`;
CREATE TABLE `register_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(20) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码(加密)',
  `nickname` varchar(20) DEFAULT NULL COMMENT '用户昵称，可为空(空时展示username)',
  `user_type` smallint DEFAULT NULL COMMENT '用户类型:1->超管,2->管理员,3->仓库管理员,4->司机,5->审核员',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `status` smallint DEFAULT NULL COMMENT '状态:0->待审核,1->已审核,2->未过审',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='注册申请';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL COMMENT '用户唯一Id',
  `username` varchar(20) DEFAULT NULL COMMENT '用户名称',
  `nickname` varchar(255) DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(255) DEFAULT NULL COMMENT '密码(加密)',
  `phone` varchar(11) DEFAULT NULL COMMENT '用户手机号',
  `user_type` varchar(255) DEFAULT NULL COMMENT '用户身份:1->super,2->manager,3->merchant,4->user,5->reviewer',
  `city` varchar(20) DEFAULT NULL COMMENT '所在城市',
  `status` smallint DEFAULT NULL COMMENT '状态:0->禁用,1->启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

-- ----------------------------
-- Mock data removed by request
-- ----------------------------
