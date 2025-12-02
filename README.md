# cosv-horizon

cosv-horizon 是一个基于 COSV Schema 的开源漏洞数据中心与协作平台，用于集中管理和共享开源软件安全漏洞信息。  
项目支持漏洞提交、审核、检索与共享，提供组织与个人的积分体系和 API 接口，方便安全团队和自动化工具集成。

> 当前部署默认仅支持 **GitHub OAuth 登录**；本地邮箱/密码注册与登录已关闭。

---

## 项目介绍

cosv-horizon 的目标是为安全研究人员、企业安全团队以及平台方提供一套统一的漏洞管理与协作平台，主要能力包括：

- **漏洞管理**
  - 按 COSV Schema 管理漏洞元数据（影响范围、时间线、修复信息等）
  - 支持表单提交与 COSV 文件导入
  - 漏洞审核、状态流转与评论讨论

- **组织与用户体系**
  - 个人用户与组织管理
  - 组织成员角色与权限控制
  - 用户与组织积分、贡献榜

- **API 与集成**
  - 基于 API Key 的访问控制
  - 支持以用户或组织身份通过 API 提交/查询漏洞
  - 预留与 AI / MCP 等系统集成能力

- **认证与安全**
  - GitHub OAuth 登录
  - JWT 鉴权与刷新机制
  - API Key 管理与使用日志
  - Redis 缓存验证码、会话等短期状态

---

## 技术栈

**后端**

- Java 17
- Spring Boot 3（Web、Security、Actuator）
- Spring Security + JWT
- MyBatis + MySQL 8
- Redis（验证码 / 会话 / 缓存）
- GitHub OAuth（基于 JustAuth）
- Springdoc OpenAPI / Swagger UI

**前端**

- Vue 3 + Vite 7
- Vuetify 3
- Vue Router 4
- Pinia 3
- Axios
- marked + DOMPurify（Markdown 渲染与防 XSS）

**基础设施与工具**

- Docker & Docker Compose（可选）
- Nginx（静态资源托管 + `/api` 反向代理到后端）
- Maven Wrapper (`./mvnw`)
- npm / Node.js

---

## 项目结构

项目采用前后端分离结构，后端为 Spring Boot 服务，前端为 Vue 3 单页应用。

```text
cosv-horizon/
├── backend/                       # 后端 Spring Boot 服务
│   ├── src/
│   │   ├── main/java/tech/cspioneer/backend/
│   │   │   ├── config/           # 安全、Web、搜索等配置（SecurityConfig 等）
│   │   │   ├── controller/       # 业务接口（Auth/OAuth/User/Org/Vulns 等）
│   │   │   ├── controller/admin/ # 管理后台接口
│   │   │   ├── security/         # JWT、API Key 认证过滤器等
│   │   │   ├── service/          # 业务服务（用户、组织、漏洞、COSV 等）
│   │   │   ├── mapper/           # MyBatis Mapper 接口
│   │   │   └── ...               
│   │   └── main/resources/
│   │       ├── application-example.properties  # 配置模板（本地覆写到 application.properties）
│   │       └── mapper/                         # MyBatis XML
│   ├── sql/
│   │   ├── schema.sql             # 数据库结构（MySQL）
│   │   └── sample_data/           # 示例数据（仅用于演示/测试）
│   ├── Dockerfile                 # 后端 Docker 构建文件
│   └── start.sh                   # 本地后台启动脚本（可选）
│
├── frontend-new/                  # 前端 Vue 3 + Vite 单页应用
│   ├── src/
│   │   ├── views/                 # 页面（登录、首页、控制台、漏洞、组织、个人中心、后台管理等）
│   │   ├── components/            # 通用组件（上传、表单片段、布局等）
│   │   ├── api/                   # 封装后的接口调用（auth/user/vuln/organization 等）
│   │   ├── stores/                # Pinia 状态管理（auth 等）
│   │   ├── router/                # 路由与路由守卫
│   │   └── main.js                # 前端入口
│   ├── nginx/default.conf         # 生产环境 Nginx 配置示例（静态+ /api 反代）
│   ├── vite.config.js             # Vite 配置（开发代理 /api → 后端）
│   └── Dockerfile                 # 前端 Docker 构建文件
│
├── docker-compose.yml             # MySQL + Redis + Backend + Web 的基础编排
├── docker-compose.override.yml    # 通过 .env 注入配置的扩展编排
├── .env.sample                    # Docker Compose 环境变量示例
├── DEPLOYMENT_GUIDE.md            # 部署指南（手动部署 + Docker）
├── CONFIG_CHECKLIST.md            # 配置检查清单（JWT、GitHub OAuth、Redis、邮件等）
├── API_KEY_TEST_GUIDE.md          # API Key 端点测试说明
└── scripts/                       # 辅助脚本（导出 schema、仓库工具等）
```

---

## 致谢

感谢 **华为云软件分析 Lab** 的技术专家对本项目的指导与建议。


