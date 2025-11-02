# 部署指南（手动部署与 Docker 部署）

本文档基于当前仓库配置，汇总后端/前端的部署方式与必备配置项，并给出可复制使用的 DockerFile 与 docker-compose 示例。建议先通读“必备依赖与配置”，再根据你的环境选择“手动部署”或“Docker 部署”。

## 1. 组件与依赖概览

- 后端：Spring Boot 3（Java 17），MyBatis，MySQL 8，Redis，邮件（SMTP），可选 Elasticsearch（默认启用 ES 搜索，可按需关闭或回退）
- 前端：Vue 3 + Vite 7 + Vuetify（Node.js ≥ 18）
- 反向代理（可选）：Nginx（用于托管前端静态资源与反代 `/api` 到后端）

## 2. 必备依赖（手动部署）

- Java 17（Temurin、Zulu 或 OpenJDK 均可）
- Maven 3.9+
- Node.js 18/20 LTS + npm
- MySQL 8.0+
- Redis 6/7
- 可选：Elasticsearch 8.x（默认启用；也可关闭或自动回退）

## 3. 关键配置项汇总（来自 backend/src/main/resources/application.properties）

可通过环境变量或命令行参数覆盖（Spring Boot 约定）：

- 服务器
  - `server.port`：默认 8080，可用 `SERVER_PORT` 覆盖
  - CORS：`cors.allowed-origins`（默认 `*`，生产建议收敛）
- 数据库（MySQL）
  - `spring.datasource.url`：默认 `jdbc:mysql://localhost:3306/cosv_horizon?...`
  - `spring.datasource.username`，`spring.datasource.password`
- Redis
  - `spring.redis.host`（默认 `localhost`），`spring.redis.port`（默认 `6379`），`spring.redis.database`（默认 `0`）
- JWT/安全
  - `security.jwt.secret`：默认 `dev-secret-change-me-please`（生产必须更换为高强度随机值）
  - `security.jwt.issuer`：默认 `cosv-horizon`
  - `security.jwt.access-ttl`：默认 `PT15M`
  - `security.jwt.refresh-ttl`：默认 `P30D`
  - 可选：`apikey.prefix`（默认 `cosv`）
- 邮件（SMTP）
  - 通过环境变量覆盖：`SMTP_HOST`、`SMTP_PORT`、`SMTP_USERNAME`、`SMTP_PASSWORD`
  - `mail.mock`：默认 `false`；可设为 `true` 跳过真实发信（开发/测试）
- OpenAPI（文档）
  - `springdoc.api-docs.path=/api-docs`，`springdoc.swagger-ui.path=/swagger-ui.html`
- 搜索引擎（Elasticsearch）
  - `search.engine=es`（可改为 `sql` 或启用 `search.es.autoFallback=true` 自动回退）
  - `search.es.enabled=true`、`search.es.uris=http://localhost:9200`、`search.es.autoBootstrap=true` 等
- Flyway 数据库迁移
  - `spring.flyway.enabled=true`、`spring.flyway.baseline-on-migrate=true`

> 强烈建议：生产环境通过环境变量或启动参数覆盖默认配置，尤其是 `security.jwt.secret`、数据库、邮件等敏感项。

## 4. 初始化数据库

1) 启动 MySQL，创建数据库并导入表结构：

```bash
mysql -u root -p -e 'CREATE DATABASE IF NOT EXISTS cosv_horizon;'
mysql -u root -p cosv_horizon < backend/sql/schema.sql
```

2) 可选：导入示例数据（便于快速演示与联调）：

```bash
# 仅用于测试/演示环境，请勿用于生产
mysql -u root -p cosv_horizon < backend/sql/sample_data/complete_vulnerability_sample_auto.sql
```

> 说明：Flyway 已启用；若你把迁移脚本放入标准位置（classpath:db/migration），也可直接依赖 Flyway。当前仓库默认更稳定的是执行 `schema.sql`。

## 5. 手动部署（后端）

1) 配置 Redis、Elasticsearch（可选）并确认可连通：

- Redis：默认 `localhost:6379/0`
- ES：默认 `http://localhost:9200`（如不使用可将 `search.engine=sql`）

2) 设置环境变量（示例，仅供参考）：

```bash
export SERVER_PORT=8080
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/cosv_horizon?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='<你的MySQL密码>'
export SPRING_REDIS_HOST='localhost'
export SPRING_REDIS_PORT='6379'
export SPRING_REDIS_DATABASE='0'
export SECURITY_JWT_SECRET='<强随机密钥>'
# 可选：邮件（或启用 mock）
export MAIL_MOCK=true
# 可选：搜索
export SEARCH_ENGINE=es
export SEARCH_ES_URIS='http://localhost:9200'
```

3) 构建 & 启动：

```bash
cd backend
./mvnw -DskipTests package
java -jar target/backend-0.0.1-SNAPSHOT.jar \
  --server.port=${SERVER_PORT} \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  --spring.redis.host=${SPRING_REDIS_HOST} \
  --spring.redis.port=${SPRING_REDIS_PORT} \
  --spring.redis.database=${SPRING_REDIS_DATABASE} \
  --security.jwt.secret=${SECURITY_JWT_SECRET} \
  --search.engine=${SEARCH_ENGINE} \
  --search.es.uris=${SEARCH_ES_URIS}
```

> 或使用仓库内脚本：`backend/start.sh`（支持 `SERVER_PORT`、`SPRING_PROFILES_ACTIVE`）。

4) 健康检查：

- Actuator：`GET http://localhost:8080/actuator/health`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`

> 注意：若 `/api-docs` 返回 500，请稍后在修复 OpenAPI 输出前，暂用 Swagger UI 或跳过 JSON 输出。

## 6. 手动部署（前端）

1) 构建：

```bash
cd frontend-new
npm ci
npm run build
# 生成的静态资源在 dist/
```

2) 用 Nginx 托管前端并反代后端（示例配置）：

```nginx
server {
  listen 80;
  server_name _;

  # 前端静态资源
  root /var/www/frontend/dist;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  # 反向代理后端 API
  location /api/ {
    proxy_pass http://backend:8080/; # 或 http://127.0.0.1:8080/
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}
```

> 若使用同机部署，可把 `proxy_pass` 指向 `http://127.0.0.1:8080/`，并将 `frontend-new/dist` 拷贝到 `/var/www/frontend/dist`。

## 7. Docker 容器化

本节提供可直接使用的 Dockerfile 与 docker-compose 参考（生产需再收敛安全与资源限制）。

### 7.1 后端 Dockerfile（多阶段构建）

```dockerfile
# backend/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

### 7.2 前端 Dockerfile（Nginx 托管静态资源）

```dockerfile
# frontend-new/Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend-new/package*.json ./
RUN npm ci
COPY frontend-new/ ./
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# 可按需在此覆盖默认 nginx.conf，配置 /api 反代到后端服务名，如 backend:8080
EXPOSE 80
```

### 7.3 docker-compose.yml（示例）

```yaml
version: '3.9'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: change-me
      MYSQL_DATABASE: cosv_horizon
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  es:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.14.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data

  backend:
    build:
      context: .
      dockerfile: backend/Dockerfile
    environment:
      SERVER_PORT: 8080
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cosv_horizon?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: change-me
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_DATABASE: 0
      SECURITY_JWT_SECRET: "please-change-me-to-a-long-random-secret"
      SEARCH_ENGINE: es
      SEARCH_ES_URIS: http://es:9200
      MAIL_MOCK: "true"
    depends_on:
      - mysql
      - redis
      - es
    ports:
      - "8080:8080"

  frontend:
    build:
      context: .
      dockerfile: frontend-new/Dockerfile
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  mysql_data:
  es_data:
```

> 初始化数据库：容器启动后，在宿主机执行 `mysql -h 127.0.0.1 -P 3306 -u root -pchange-me cosv_horizon < backend/sql/schema.sql`。

## 8. 生产配置建议与密钥管理

- 强制更换：`SECURITY_JWT_SECRET`、数据库密码、Nginx 站点配置等
- 针对邮件：建议使用真实 SMTP 并禁用 `MAIL_MOCK`
- OAuth（GitHub 登录，可选）：
  - `oauth.github.client-id`，`oauth.github.client-secret`，`oauth.github.redirect-uri`（需与 GitHub 应用一致）
  - 前端回跳：`oauth.frontend.finish-uri`
- CORS：将 `cors.allowed-origins` 从 `*` 收敛为你的前端域名
- 资源限制：在 docker-compose/容器编排平台中设置 CPU/内存限制与健康检查

## 9. 常见问题

- OpenAPI JSON（`/api-docs`）返回 500：
  - 先使用 Swagger UI（`/swagger-ui/index.html`）或后续修复 openapi 输出
- 使用 API Key 调用 `/cosv/**`：
  - 需 `vuln:write` scope；以组织身份时需组织 ADMIN，且组织 ACTIVE
  - 详见根目录 `API_KEY_TEST_GUIDE.md`
- 注册/重置密码邮件：
  - 开发可设 `MAIL_MOCK=true` 跳过真实发信；生产请配置 SMTP 并置 `MAIL_MOCK=false`

## 10. 验证自检

- 登录：`POST /auth/login`
- 我：`GET /users/me`（Bearer）
- 漏洞搜索：`GET /vulns/search`（Bearer 或带 `vuln:read` 的 API Key）
- 健康：`GET /actuator/health`

至此，手动与 Docker 部署均可完成。如需我把 docker-compose 与 Dockerfile 直接落盘到仓库相应目录，或补充 Nginx 配置文件模板，请告诉我。
