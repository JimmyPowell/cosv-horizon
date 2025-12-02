# 部署配置清单（不含数据库账号密码）

本清单汇总了部署本项目（cosv-horizon）时需要关注的**非数据库类敏感配置**，以便在本地开发、测试和生产环境中逐项确认。建议结合 `backend/src/main/resources/application.properties`、`docker-compose.yml` 与 `docker-compose.override.yml` 一起使用。

---

## 1. JWT 鉴权配置

- `security.jwt.secret`
  - 用途：签发与校验访问/刷新 Token，以及 OAuth state 签名。
  - 建议：生产环境必须为高强度随机字符串，通过环境变量或外部配置注入（Docker 中对应 `SECURITY_JWT_SECRET`）。
- `security.jwt.issuer`
  - 用途：JWT 发行方标识。
  - 默认：`cosv-horizon`，可按实际品牌调整。
- `security.jwt.access-ttl`
  - 用途：访问 Token 有效期，ISO-8601 Duration，如 `PT15M`。
- `security.jwt.refresh-ttl`
  - 用途：刷新 Token 有效期，如 `P30D`。
- `security.jwt.token-prefix`
  - 用途：HTTP 头中 Authorization Bearer 前缀，一般保持默认 `Bearer ` 即可。

> 生产建议：通过环境变量或外部配置中心注入 JWT Secret，不要使用示例值。

---

## 2. 邮件发送（SMTP）配置

> 负责注册验证、找回密码等邮件通知。

核心属性（`application.properties` + Docker 环境变量）：

- `spring.mail.host` / `SMTP_HOST`
  - 用途：SMTP 服务器地址（如 `smtp.qq.com`、`smtp.gmail.com`）。
- `spring.mail.port` / `SMTP_PORT`
  - 用途：SMTP 端口（常见：`465` SSL、`587` STARTTLS）。
- `spring.mail.username` / `SMTP_USERNAME`
  - 用途：登录 SMTP 的账号，亦常用作发件人邮箱。
- `spring.mail.password` / `SMTP_PASSWORD`
  - 用途：SMTP 密码或授权码。
- `spring.mail.protocol`
  - 用途：协议，通常为 `smtp`。
- `spring.mail.properties.mail.smtp.auth`
  - 用途：是否启用身份认证，通常为 `true`。
- `spring.mail.properties.mail.smtp.ssl.enable`
  - 用途：是否启用 SSL。
- `spring.mail.properties.mail.smtp.starttls.required`
  - 用途：是否强制 STARTTLS（与端口搭配使用）。
- `mail.mock`
  - 用途：是否启用邮件“模拟模式”。
  - 说明：`true` 时不实际发信，仅打印日志；生产应设为 `false`。

待补充信息：

- [ ] 选择生产 SMTP 服务商（域名、端口、协议）。
- [ ] 为平台准备专用发件邮箱与授权码。
- [ ] 在生产环境中设置上述全部 SMTP 参数。
- [ ] 确认 `mail.mock=false`，并进行实际收信验证。

---

## 3. GitHub OAuth 登录配置

> 相关代码：`backend/src/main/java/tech/cspioneer/backend/controller/OAuthController.java`  
> 属性定义：`backend/src/main/resources/application.properties`

必填属性：

- `oauth.github.client-id` / `OAUTH_GITHUB_CLIENT_ID`
  - 用途：GitHub OAuth 应用的 Client ID。
- `oauth.github.client-secret` / `OAUTH_GITHUB_CLIENT_SECRET`
  - 用途：GitHub OAuth 应用的 Client Secret。
- `oauth.github.redirect-uri` / `OAUTH_GITHUB_REDIRECT_URI`
  - 用途：GitHub 回调地址（如 `https://your-domain/oauth/github/callback` 或 `http://localhost:8080/oauth/github/callback`）。
  - 要求：需与 GitHub OAuth 应用中配置的回调 URL 完全一致。
- `oauth.frontend.finish-uri` / `OAUTH_FRONTEND_FINISH_URI`
  - 用途：前端在 OAuth 完成后跳转的地址（如 `https://your-domain/oauth/finish`）。
- `oauth.github.scopes`
  - 用途：GitHub OAuth 授权 scope 列表，默认 `read:user,user:email`。

待补充信息：

- [ ] 创建 GitHub OAuth App，并记录 `client-id` / `client-secret`。
- [ ] 按部署域名设置 `redirect-uri` 与前端 `finish-uri`。
- [ ] 在环境变量或配置文件中填入上述四项。
- [ ] 在本地和生产环境各自完成一次登录/绑定测试。

---

## 4. Redis 配置

> 用于验证码、会话等短期状态存储。

属性及环境变量：

- `spring.redis.host` / `SPRING_REDIS_HOST`
- `spring.redis.port` / `SPRING_REDIS_PORT`
- `spring.redis.database` / `SPRING_REDIS_DATABASE`
- （Docker override 中额外）`spring.data.redis.*` / `SPRING_DATA_REDIS_*`

待补充信息：

- [ ] 选定生产 Redis 实例地址、端口及数据库编号。
- [ ] 若启用密码或 TLS，补充对应 Spring Boot 配置。
- [ ] 在生产环境中完成连通性及性能验证。

---

## 5. 邮件验证码与会话 TTL 配置

> 相关服务：`VerificationCodeService`、`PasswordResetCodeService`、`RegistrationSessionService`、`PasswordResetSessionService`。

- `auth.code.ttl`
  - 用途：验证码有效期（如 `PT10M` 表示 10 分钟）。
- `auth.session.ttl`
  - 用途：注册/重置密码会话有效期（如 `PT15M`）。
- `auth.rate.email.per-minute`
  - 用途：同一邮箱每分钟最多发送验证码次数。
- `auth.rate.email.per-day`
  - 用途：同一邮箱每天最多发送验证码次数。
- `auth.rate.ip.per-minute`
  - 用途：同一 IP 每分钟触发验证码发送的上限。
- `auth.cooldown-seconds`
  - 用途：发送验证码后的冷却时间（秒）。

待补充信息：

- [ ] 根据实际安全策略调整 TTL 与限流阈值。
- [ ] 在生产环境开启邮件限流，并验证行为是否符合预期。

---

## 6. CORS（跨域）配置

> 相关类：`backend/src/main/java/tech/cspioneer/backend/config/WebConfig.java`

- `cors.allowed-origins`
  - 用途：允许的前端来源列表，逗号分隔。
  - 默认：`*`（开发期方便调试，不建议在生产中使用）。
- `cors.allowed-methods`
  - 用途：允许的 HTTP 方法列表。
- `cors.allowed-headers`
  - 用途：允许的请求头列表，如 `Authorization,Content-Type,X-API-Key`。

待补充信息：

- [ ] 根据生产前端域名设置精确的 `allowed-origins`（如 `https://your-domain`）。
- [ ] 如有多域名/子域名，统一列出或考虑使用通配符策略。

---

## 7. 搜索引擎 / Elasticsearch / OpenSearch 配置

> 默认使用 SQL 搜索；如需启用 ES/OpenSearch，请配置以下属性。  
> 参考：`backend/src/main/resources/application.properties`、`backend/src/main/java/tech/cspioneer/backend/search/SearchConfig.java`

核心属性：

- `search.engine`
  - 用途：搜索引擎类型，常见为 `es` 或 `sql`。
- `search.es.enabled`
  - 用途：是否启用 ES/OpenSearch 集成。
- `search.es.uris`
  - 用途：ES/OpenSearch 节点 URI 列表，逗号分隔（例如 `http://es1:9200,http://es2:9200`）。
- `search.es.index.prefix`
  - 用途：索引前缀。
- `search.es.index.version`
  - 用途：索引版本标识（如 `v1`）。
- `search.es.index.read-alias` / `search.es.index.write-alias`
  - 用途：读/写别名，用于滚动升级索引。
- `search.es.index.shards` / `search.es.index.replicas`
  - 用途：分片与副本数。
- `search.es.autoBootstrap` / `search.es.bootstrapReindex` / `search.es.autoFallback`
  - 用途：是否自动创建索引、重建索引、自动降级到 SQL。

待补充信息（仅在启用 ES/OpenSearch 时）：

- [ ] 选定 ES/OpenSearch 集群地址与安全策略（认证、TLS）。  
- [ ] 设置 URI、索引前缀、分片/副本等配置。  
- [ ] 在非生产环境验证索引创建和搜索功能后再开启生产。

---

## 8. 前端与后端 URL / 反向代理配置

> 前端：`frontend-new/vite.config.js`、`frontend-new/nginx/default.conf`  
> 后端：`server.port`、Docker `backend` 服务暴露端口

- 开发环境：
  - Vite 开发服务器通过 `/api` 代理到后端 `http://localhost:8080`。
- 生产环境：
  - Nginx 容器（`web`）在 `80` 端口对外提供前端静态资源，并通过 Nginx 配置反代 `/api` 到后端。

待补充信息：

- [ ] 确定生产域名与端口（是否启用 HTTPS/TLS 终止）。  
- [ ] 根据实际部署拓扑调整 Nginx 反代地址和 `oauth.frontend.finish-uri`。  
- [ ] 若前后端分离部署，确保 CORS 与代理设置一致。

---

## 9. API Key 与注册策略（可选）

> 部分配置通过代码或数据库策略控制，这里列出可能需要关注的开关。

- `REGISTRATION_FIRST_USER_ADMIN`（docker-compose.override 中）
  - 用途：首个注册用户是否自动授予 ADMIN 权限。
  - 建议：首环境初始化后设为 `false`。

待补充信息：

- [ ] 明确初始管理员账号创建流程（邮件注册链接 / 手动插库）。  
- [ ] 决定是否开放公共注册，及是否需要额外接入验证码/风控。

---

## 10. 其他可能的外部集成（预留）

目前代码中未发现如对象存储（OSS/MinIO）、第三方日志/监控平台等必填配置。但在生产环境通常还需要：

- [ ] 应用日志采集与持久化方案（如 Loki/ELK/云厂商日志服务）。  
- [ ] 指标与健康检查接入（已开放 Actuator `health,info`，可接入 Prometheus 等）。  
- [ ] HTTPS 证书与终端用户访问域名规划。  

如未来在代码中引入新的外部服务（如 GitLab OAuth、企业邮箱网关、对象存储），建议将对应的配置项追加到本清单中，保持部署文档与实现同步。

