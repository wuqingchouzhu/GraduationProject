# 进销存系统改进计划

## 整体分析

当前项目是一个 Spring Boot 3.5.1 + MyBatis-Plus + RabbitMQ + MySQL 的进销存管理系统。经分析，部分改进项（如环境变量）已基本满足要求，部分需要新增或修改。以下按优先级顺序排列。

---

## P0 — 必须修复（答辩前必做）

### 1. 敏感信息检查与环境变量确认 ✅ 基本已满足

**当前状态**：
- `application.properties` 已使用 `${DB_PASSWORD:}` 和 `${RABBITMQ_PASSWORD:guest}` 占位符格式
- Banner 打印在 `DemoApplication.java` 中，无任何密码泄露

**需做的微调**：
- 检查 `${DB_PASSWORD:}` 默认值是否合理（当前为空，防止无环境变量时连不上）
- 确认 banner 中的 `System.out.println` 确实无密码

**执行步骤**：
1. 读取 `application.properties` 确认占位符格式
2. 读取 `DemoApplication.java` 确认 banner 内容不包含密码
3. 如有需要，微调即可；大概率无需改动

---

### 2. 删除 DBUtil.java 🗑️

**当前状态**：已确认 `DBUtil.java` 在整个项目中**没有任何类引用**，属于死代码。其依赖的 `db.properties` 文件也不存在。

**执行步骤**：
1. 删除文件 `src/main/java/com/qiao/demo/inventory/utils/DBUtil.java`
2. 如果 `utils` 目录下只剩这一个文件，同时删除 `utils` 空目录

---

## P1 — 架构优化（强烈建议）

### 3. 新增全局异常处理器

**目标**：新建 `GlobalExceptionHandler`，统一返回 `Result.error(...)` 格式，替代 Spring Boot 默认 `/error` JSON 格式。

**执行步骤**：
1. 新建 `src/main/java/com/qiao/demo/inventory/common/GlobalExceptionHandler.java`
2. 使用 `@RestControllerAdvice`
3. 处理方法：
   - `BusinessException` → 返回 `Result.error(e.getMessage())`，HTTP 状态码用 e.getCode()
   - `MethodArgumentNotValidException` → 提取校验失败字段信息，返回 `Result.error(拼接的校验信息)`，HTTP 400
   - `RuntimeException` → 返回 `Result.error("系统内部错误")`，HTTP 500，同时打印日志
   - `Exception` → 兜底，返回 `Result.error("未知错误")`，HTTP 500

**涉及文件**：
- 新建：`GlobalExceptionHandler.java`

---

### 4. 添加 Controller 参数校验

**目标**：为所有 `@RequestBody` 参数添加 `@Valid` 校验，DTO 加上校验注解。

**执行步骤**：
1. 在 `pom.xml` 中添加 `spring-boot-starter-validation` 依赖（Spring Boot 3.x 中需显式引入）
2. 修改 `StockOutDTO`：添加 `@NotNull` 到 `productId`、`quantity`，添加 `@Min(1)` 到 `quantity`
3. 修改 `AuthController.login()` 的请求参数：如果当前使用 Map 接收，封装为 DTO 并加 `@NotBlank`
4. 修改 `ProductController`：为所有接收 `@RequestBody` 的方法参数添加 `@Valid`
   - `stockIn()` 入参（当前可能是 Product 或 Map，需要确认）
   - `stockOut()` 入参（`@RequestBody StockOutDTO`）
   - `addProduct()` 入参
5. 结合步骤 3 的 `GlobalExceptionHandler` 统一返回校验错误信息

**涉及文件**：
- 修改：`pom.xml`
- 修改：`StockOutDTO.java`
- 修改：`ProductController.java`
- 修改：`AuthController.java`
- 可能需要新建：`LoginDTO.java`（如当前用 Map 接收则新建）

---

### 5. 出库消费端消息幂等性保护 🔒

**目标**：防止 RabbitMQ 重复投递导致库存重复扣减。

**推荐方案 A**：`messageId` 预查法（在 `StockOutDTO` 中添加 `messageId` 字段）

**执行步骤**：
1. 修改 `StockOutDTO.java`：添加 `private String messageId;` 字段
2. 修改 `StockOutProducer.send()`：在发送前调用 `dto.setMessageId(UUID.randomUUID().toString())`
3. 在数据库 `stock_out_record` 表中添加 `message_id VARCHAR(36)` 字段和 `UNIQUE` 索引
4. 修改 `StockOutConsumer` 的消费逻辑：处理前先查 `stock_out_record` 表中是否已存在该 `messageId`，存在则直接 ACK 跳过（打印日志），不存在则执行业务逻辑后插入记录
5. 提供 SQL 迁移脚本（可选，添加到 `resources/sql/`）

**涉及文件**：
- 修改：`StockOutDTO.java`
- 修改：`StockOutProducer.java`
- 修改：`StockOutConsumer.java`
- 修改：`ProductServiceImpl.stockOut()`（插入流水时写入 messageId）
- 新建：`resources/sql/migration_add_message_id.sql`（可选）

---

### 6. 集成 SpringDoc (Swagger) 生成 API 文档

**目标**：提供可视化 API 文档，可通过浏览器访问。

**执行步骤**：
1. 在 `pom.xml` 中添加 `springdoc-openapi-starter-webmvc-ui` 依赖（版本与 Spring Boot 3.5.1 兼容，如 `2.7.0` 或 `2.8.0`）
2. 在三个 Controller 类上添加 `@Tag(name = "...")` 注解：
   - `AuthController` → `@Tag(name = "认证接口")`
   - `ProductController` → `@Tag(name = "商品管理")`
   - `AdminController` → `@Tag(name = "管理员接口")`
3. 为 Controller 方法添加 `@Operation(summary = "...")` 注解
4. 为实体类（`Product`、`StockOutDTO`、`User`、`Result`）添加 `@Schema(description = "...")` 注解
5. 确认 `http://localhost:8080/swagger-ui.html` 可访问

**涉及文件**：
- 修改：`pom.xml`
- 修改：`AuthController.java`
- 修改：`ProductController.java`
- 修改：`AdminController.java`
- 修改：`Product.java`
- 修改：`StockOutDTO.java`
- 修改：`User.java`
- 修改：`Result.java`

---

### 7. 统一 MQ 使用策略 — 入库也加 MQ（方案 A）

**问题**：入库是同步调用，出库走 RabbitMQ 异步，设计不一致，答辩易被质疑。

**选定方案 A**：保留 MQ，入库也拆成 `StockInProducer` + `StockInConsumer`，让入库/出库设计完全对称。

**执行步骤**：
1. 新建 `StockInDTO`（`src/main/java/com/qiao/demo/inventory/dto/StockInDTO.java`）：
   - 字段：`productId`、`quantity`、`messageId`（用于幂等性）
   - 实现 `Serializable`
2. 新建 `StockInProducer`（`src/main/java/com/qiao/demo/inventory/mq/StockInProducer.java`）：
   - `send(StockInDTO dto)` 方法，发送到入库交换机
   - 发送前设置 `dto.setMessageId(UUID.randomUUID().toString())`
3. 新建 `StockInConsumer`（`src/main/java/com/qiao/demo/inventory/mq/StockInConsumer.java`）：
   - 监听入库队列，调用 `productService.stockIn(productId, quantity)`
   - 手动 ACK/NACK 策略与 `StockOutConsumer` 一致
   - 幂等性保护：处理前先查 `stock_in_record` 表中 `message_id` 是否存在
4. 新建 `StockInDlqConsumer`（`src/main/java/com/qiao/demo/inventory/mq/StockInDlqConsumer.java`）：
   - 监听入库死信队列，持久化失败消息到 `failed_messages` 表
5. 修改 `RabbitConfig.java`：添加入库相关的交换机、队列、绑定、死信配置
   - 新增 `STOCK_IN_EXCHANGE`、`STOCK_IN_QUEUE`、`STOCK_IN_ROUTING_KEY`
   - 新增 `STOCK_IN_DLX_EXCHANGE`、`STOCK_IN_DLQ_QUEUE`、`STOCK_IN_DLQ_ROUTING_KEY`
   - 同时为出库部分也添加对应的消息幂等所需的支持
6. 修改 `ProductController.stockIn()`：改为调用 `stockInProducer.send(dto)` 发送入库消息
7. 更新 `failed_messages` 表结构：添加 `message_id VARCHAR(36)` 字段和 `idx_message_id` 索引（用于幂等性排查）
8. `AdminController` **保留**，继续提供失败消息管理功能（入库+出库失败消息统一管理）
9. `stock_out_record` 和 `stock_in_record` 表添加 `message_id VARCHAR(36)` 字段 + UNIQUE 索引（见任务5的交叉部分）

**涉及文件**：
- 新建：`StockInDTO.java`
- 新建：`StockInProducer.java`
- 新建：`StockInConsumer.java`
- 新建：`StockInDlqConsumer.java`
- 修改：`RabbitConfig.java`
- 修改：`ProductController.java`
- 修改：`AdminController.java`（支持入库失败消息类型）
- 修改：`resources/sql/failed_messages.sql`（添加 message_id 字段）

---

## P2 — 锦上添花（时间充裕再搞）

### 8. 定时任务分布式锁预留注释

**执行**：在 `ProductServiceImpl` 的 `@Scheduled dailyStatisticsTask()` 方法上加注释说明多实例部署时需引入分布式锁。

---

### 9. 补充核心单元测试

**执行**：在 `src/test/java/com/qiao/demo/` 下新建 `ProductServiceTest.java`，测试：
- `stockIn()` 正常入库 → 库存增加 + 流水记录
- `stockOut()` 正常出库 → 库存减少 + 流水记录
- `stockOut()` 库存不足 → 抛 `BusinessException`
- `stockOut()` 并发冲突 → 乐观锁重试失败

使用 Spring Boot Test + `@SpringBootTest` + Mock 或 H2 内存数据库。

---

### 10. 控制器返回状态细化

**执行**：修改 `/api/product/stockOut` 的返回信息；如果选了方案 B（去 MQ），出库变同步，可直接返回实际结果；无需异步提示。

---

### 11. 登录加 JWT

**执行**：
1. 添加 `spring-boot-starter-security` 和 `jjwt` 依赖
2. 新建 `JwtUtil` 工具类
3. 修改 `AuthController.login()` 返回 JWT token
4. 使用 BCrypt 加密存储密码
5. 添加 `SecurityConfig` 配置
6. 前端 `App.vue` 存储 token 并在后续请求头中携带

**注意**：此项改动较大，建议在导师要求时才做。

---

### 12. Lombok 化

**执行**：为 `StockOutDTO`、`Product`、`User`、`Result` 中手写的 getter/setter/constructor 替换为 `@Data`、`@AllArgsConstructor`、`@NoArgsConstructor`。

---

### 13. 完善前端

**执行**：在 `inventory-frontend/` 中补充与后端接口实际联调的页面组件。

---

## P0+P1 实施范围确认

> **本次实施范围**：仅 P0（2项）+ P1（5项）= 共 **7 项**。P2 各项保留计划供后续参考，本次不实施。

---

## 建议实施顺序

| 顺序 | 任务 | 优先级 | 影响范围 |
|------|------|--------|----------|
| 1 | 确认环境变量 + Banner 无密码 | P0 | 小 |
| 2 | 删除 DBUtil.java | P0 | 小 |
| 3 | 新增全局异常处理器 | P1 | 中 |
| 4 | Controller 参数校验 | P1 | 中 |
| 5 | 消息幂等性保护（入库+出库） | P1 | 中 |
| 6 | 统一 MQ 策略（入库也加 MQ） | P1 | **大** |
| 7 | 集成 Swagger (SpringDoc) | P1 | 小 |

> **注意**：任务 5（幂等性）和任务 6（入库加 MQ）有交叉 — 新建的 `StockInConsumer` 同样需要幂等性保护。建议先完成任务 6 的框架搭建，再统一追加任务 5 的幂等性逻辑。
