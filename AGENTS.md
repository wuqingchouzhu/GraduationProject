# AGENTS.md — 进销存管理系统

## Project Location

The real project is `demo/`, **NOT** the root directory. The root `src/Main.java` is a stale placeholder.

All commands below assume `demo/` as working directory.

## Quick Start

```bash
# Backend (from demo/)
cp ../.env.example .env          # fill in DB/RabbitMQ credentials
./mvnw spring-boot:run           # starts on :8080

# Frontend (from demo/inventory-frontend/)
npm install
npm run serve                    # starts on :8081
```

## Tech Stack

| Layer | Tech |
|---|---|
| Backend | Java 17, Spring Boot 3.5.1, Maven (wrapper) |
| ORM | MyBatis-Plus 3.5.10.1 (BaseMapper, lambda queries) |
| DB | MySQL (`inventory_db`) |
| MQ | RabbitMQ (stock in/out async + DLQ) |
| API Doc | SpringDoc 2.7.0 → `http://localhost:8080/swagger-ui.html` |
| Frontend | Vue 3 (Composition API), Element-Plus, Axios, Vue CLI 5 |

## Project Structure

```
demo/
├── pom.xml                          # Spring Boot parent 3.5.1
├── src/main/java/com/qiao/demo/
│   ├── DemoApplication.java         # @SpringBootApplication + @EnableScheduling
│   └── inventory/
│       ├── common/                  # Result<T>, BusinessException, GlobalExceptionHandler
│       ├── config/                  # MybatisPlusConfig (optimistic lock), RabbitConfig, UserContext
│       ├── controller/              # AuthController, ProductController, AdminController
│       ├── dao/                     # ProductMapper, UserMapper (MyBatis-Plus BaseMapper)
│       │   ├── impl/                # ProductServiceImpl ← THE main service (330 lines)
│       │   └── ProductDao.java      # ⚠️ DEAD CODE — manual JDBC, zero references, not used
│       ├── dto/                     # LoginDTO, StockInDTO, StockOutDTO (with @Valid annotations)
│       ├── model/                   # Product (product_info), User (sys_user)
│       ├── mq/                      # StockIn/StockOut Producer + Consumer + DlqConsumer
│       └── service/                 # ProductService, UserService interfaces
├── src/main/resources/
│   ├── application.properties       # ${DB_PASSWORD:} — env-var placeholders
│   └── sql/                         # DDL + migration scripts
└── inventory-frontend/
    └── src/App.vue                  # Single-file SPA (login + product table all in one)
```

## Architecture: Stock In/Out Flow

Both stock-in and stock-out are **asynchronous via RabbitMQ**:

1. Controller validates DTO (`@Valid`), checks product existence + stock sufficiency (for out)
2. Sends to MQ via `StockInProducer`/`StockOutProducer` (sets `messageId = UUID`)
3. Consumer picks up → `ProductServiceImpl.stockIn/productId, qty, messageId)`
4. On `BusinessException` → ACK (no retry, business logic error)
5. On `RuntimeException` → NACK → DLQ → `failed_messages` table

**Idempotency**: Both consumers check `stock_in_record.message_id` / `stock_out_record.message_id` before processing. If duplicate → ACK skip.

## Key Design Decisions

- **Optimistic locking**: `@Version` on `Product.version`, plugin in `MybatisPlusConfig`
- **Dynamic reorder point**: Formula `R = avg_daily_demand × lead_time + 1.65 × demand_std_dev × √(lead_time)`. Recalculated daily at 2 AM via `@Scheduled`.
- **No JWT/auth security**: Login is plaintext username/password comparison against `sys_user` table. `UserContext` uses a static field (thread-unsafe — only works in single-user demo mode).
- **Global error handling**: `GlobalExceptionHandler` catches `BusinessException`, `MethodArgumentNotValidException`, `RuntimeException`, and `Exception` → returns `Result<T>`.

## Warnings & Gotchas

- ⚠️ **`ProductDao.java` is dead code.** The project uses MyBatis-Plus `ProductMapper` (which extends `BaseMapper<Product>`). `ProductDao` has zero references — delete it safely.
- ⚠️ **Frontend payload mismatch**: `App.vue` sends `{id, quantity}` for stockIn/stockOut, but the backend DTOs expect `{productId, quantity}`. The `id` field is silently ignored by Jackson. Fix the frontend payload keys.
- ⚠️ **Frontend hardcodes `localhost:8080`**. No env-based API URL configuration.
- ⚠️ **No `.env` in repo** (gitignored). `.env.example` at root requires copying + filling credentials.
- ⚠️ **`UserContext` is NOT thread-safe.** Uses static `currentUser` field — fine for demo, breaks with concurrent users.
- ⚠️ **Scheduled task lacks distributed lock.** Acknowledged in improvement plan — add distributed lock if deploying multiple instances.
- The `.gitignore` at repo root may be binary/corrupted. The real `.gitignore` is at `demo/.gitignore`.

## Commands

```bash
# --- Backend (from demo/) ---
./mvnw spring-boot:run                          # start backend
./mvnw test                                      # run tests (needs DB running)
./mvnw test -Dtest="DemoApplicationTests"         # run single test class
./mvnw clean package                             # build JAR

# --- Frontend (from demo/inventory-frontend/) ---
npm run serve                                    # dev server :8081
npm run build                                    # production build
npm run lint                                     # lint

# --- Database setup ---
# 1. Create DB: CREATE DATABASE inventory_db;
# 2. Run SQL scripts in demo/src/main/resources/sql/
#    - failed_messages.sql
#    - migration_add_message_id.sql
# 3. Tables expected: product_info, sys_user, stock_in_record, stock_out_record, failed_messages
```

## Prerequisites

- Java 17+
- MySQL 8+ with database `inventory_db`
- RabbitMQ (default port 5672)
- Node.js 16+ (for frontend)

## Key Reference

- `.trae/documents/improvement-plan.md` — authoritative improvement plan with P0/P1/P2 priorities. Already partially implemented (Swagger, validation, global exception handler, MQ symmetry, idempotency).
- `HELP.md` — auto-generated Spring Boot starter docs (low signal, safe to delete).
