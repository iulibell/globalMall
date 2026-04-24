# globalMall

商城侧微服务示例：门户（`mall-portal`）、运营后台（`mall-admin`）、网关、认证、系统用户、消息、商品检索等模块，通过 **Nacos** 注册发现，**OpenFeign** 服务调用；网关可转发到本仓库未包含的物流服务（`/oms/**`、`/wms/**`），需自行启动或改路由。

## 技术栈

| 类别 | 说明 |
|------|------|
| 运行时 | Java 17 |
| 框架 | Spring Boot 4.x（父 POM）、Spring Cloud Gateway、Spring Data Redis 等 |
| 数据访问 | MyBatis-Plus、MySQL 8 |
| 注册发现 | Spring Cloud Alibaba Nacos Discovery |
| 认证 | Sa-Token + JWT（配置见各模块 `application.yml`） |
| 消息 | Spring AMQP（RabbitMQ） |
| 检索 | Spring Elasticsearch（`gl-search`） |

## 模块说明

| 模块 | 说明 |
|------|------|
| `gl-common` | 公共组件与工具 |
| `gl-gateway` | 统一入口与路由 |
| `gl-auth` | 认证相关接口 |
| `gl-system` | 系统用户、登录注册等 |
| `mall-admin` | 运营后台业务 |
| `mall-portal` | C 端门户业务 |
| `gl-message` | 消息服务 |
| `gl-search` | 商品搜索（Elasticsearch） |
| `frontend` | Vue 3 + Vite 前端（可选） |

## 环境依赖

本地开发建议准备：

1. **JDK 17**
2. **Maven 3.9+**
3. **MySQL 8.x**，库名 **`global_mall`**（与 `application.yml` 中 JDBC 一致）
4. **Redis**（默认 `127.0.0.1:6379`）
5. **Nacos**（默认 `127.0.0.1:8848`，`DEFAULT_GROUP`）
6. **RabbitMQ**（`mall-admin`、`gl-message` 使用；默认 `127.0.0.1:5672`，guest/guest）
7. **Elasticsearch**（`gl-search`；默认 `http://127.0.0.1:9200`）

数据库表结构需按团队脚本或实体自行初始化；本目录未附带 SQL 文件。

### 使用 Docker Compose 起中间件

在项目根目录（本 `README.md` 所在目录）执行：

```bash
docker compose up -d
```

该编排与默认配置对齐：**root / 123456**、库 **`global_mall`**、Redis **6379**、Nacos **8848**、RabbitMQ **5672**（管理台 **15672**）、Elasticsearch **9200**。

停止并移除容器（保留数据卷）：

```bash
docker compose down
```

在宿主机启动各 Spring Boot 服务时，将 `NACOS_SERVER_ADDR`、`spring.datasource` 等指向 `127.0.0.1` 即可（端口已映射到本机）。

## 默认服务端口

| 服务 | 默认端口 |
|------|----------|
| `gl-gateway` | 8202（`GATEWAY_PORT`） |
| `gl-auth` | 8201 |
| `gl-message` | 8203（`SERVER_PORT`） |
| `gl-search` | 8204 |
| `gl-system` | 8205 |
| `mall-admin` | 8206 |
| `mall-portal` | 8207 |

## 网关路由（摘录）

网关默认端口 **8202**。常见前缀：

| 路径前缀 | 目标服务 |
|----------|----------|
| `/system/**` | `mall-gl-system` |
| `/auth/**` | `mall-gl-auth` |
| `/admin/**` | `mall-admin` |
| `/portal/**` | `mall-portal` |
| `/oms/**`、`/wms/**` | 外部 `logi-oms` / `logi-wms`（需 Nacos 中存在对应实例或配置直连 URI） |

无 Nacos 时可在 `gl-gateway` 的 `application.yml` 中用 `SERVICE_URI_*` 指向 `http://host:port`。

## 常用环境变量（与 `application.yml` 一致）

| 变量 | 含义 |
|------|------|
| `NACOS_SERVER_ADDR` | Nacos 地址，如 `127.0.0.1:8848` |
| `NACOS_GROUP` | 分组，默认 `DEFAULT_GROUP` |
| `GATEWAY_PORT` | 网关端口 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` / `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | RabbitMQ |
| `ELASTICSEARCH_URIS` | ES 地址，如 `http://127.0.0.1:9200` |
| `SECURITY_INTERNAL_TOKEN` | 内部回调令牌（生产请启用并配置） |

## 构建后端

```bash
mvn clean install -DskipTests
```

仅编译示例：

```bash
mvn -DskipTests compile
```

各可运行模块的主类位于对应模块的 `*Application.java`。

## 前端（可选）

目录：`frontend/`（Node 见 `frontend/package.json` 的 `engines`）。

```bash
cd frontend
npm install
npm run dev
```
