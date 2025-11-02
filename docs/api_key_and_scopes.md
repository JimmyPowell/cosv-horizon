# API Key 使用与权限（Scopes）

本文说明后端当前对 API Key 的支持范围、授权模型与各端点可访问性。

## 概览

- 头部传递位置与格式
  - 优先读取 `X-API-Key`，否则读取 `Authorization`
  - 支持两种写法：
    - 直接传 token：`cosv_<prefix>_<secret>`
    - 或者：`ApiKey cosv_<prefix>_<secret>`（带前缀）
- 身份与权限
  - 身份主体为创建该 Key 的用户 UUID
  - 根据 Key 的 `scopes` 注入 `SCOPE_<scope>` 权限（例如 `SCOPE_vuln:read`）
  - 注入 `ROLE_API`（注意：不会注入 `ROLE_USER/ROLE_ADMIN`）
  - 若 Key 绑定了组织，运行时会有 `subjectType=ORG, orgUuid=<组织UUID>` 的上下文；否则为 `USER`（个人 PAT）
- 组织范围限制
  - 组织级 Key（subjectType=ORG）：只能在该组织范围内读/写；跨组织将被拒绝（服务层 `enforceOrgScopeUuid`）
  - 个人 PAT：不受组织范围限制，但仍需具备相应 scope

## 支持的 Scopes 列表

`vuln:read`、`vuln:write`、`org:read`、`org:write`、`notification:read`

> 提示：创建/管理 API Key 的端点本身仅支持 JWT 登录用户，不支持使用 API Key 调用。

## 端点访问矩阵

### 漏洞（vuln:read / vuln:write）

- 读（需要 `vuln:read`）
  - `GET /vulns`（分页过滤）
  - `GET /vulns/search`（高级过滤，支持 `q` 统一搜索：编号前缀、别名前缀、名称模糊）
  - `GET /vulns/{uuid}`（详情）
  - `GET /tags`、`GET /tags/{uuid}/usage`
  - `GET /categories`、`GET /categories/{uuid}/usage`
- 写（需要 `vuln:write`）
  - `POST /vulns`（创建；组织 Key 必须指定且只能写入自己的组织）
  - `PATCH /vulns/{uuid}`（更新）
  - `POST /vulns/{uuid}/tags`、`DELETE /vulns/{uuid}/tags/{name}`
  - `POST /vulns/{uuid}/projects`、`DELETE /vulns/{uuid}/projects/{projectUuid}`
- 组织 Key 限制：仅能读写所属组织的资源；跨组织会被拒绝。

### 组织（org:read / org:write）

- 读（需要 `org:read`）
  - `GET /orgs/me`（我的组织列表；为 Key 创建者用户）
  - `GET /orgs/{uuid}`（组织详情）
  - `GET /orgs/{uuid}/members`（需成员身份；服务层验证）
  - `GET /orgs/{uuid}/members/public`（公开组织公开成员列表）
  - `GET /orgs/search`（公开组织搜索）
  - `GET /orgs/{uuid}/invite-links`（需管理员；服务层验证）
  - `GET /orgs/{uuid}/invitations`（组织管理员视角；服务层验证）
  - `GET /orgs/{uuid}/points`（组织积分；服务层验证）
- 写（需要 `org:write`）
  - `PATCH /orgs/{uuid}`（更新基础信息；需管理员）
  - `POST /orgs/{uuid}/members/change-role`、`POST /orgs/{uuid}/members/remove`（需管理员）
  - `POST /orgs/{uuid}/invite-links`、`POST /orgs/invite-links/{linkUuid}/revoke`（需管理员）
  - `POST /orgs/invitations/{inviteUuid}/accept`、`POST /orgs/invitations/{inviteUuid}/reject`（仅对发给自己的邀请有效）
  - `POST /orgs/{uuid}/actions/disband`（解散组织；需管理员）
- 不支持用组织 Key 创建组织：`POST /orgs` 会被拒绝。

### 通知（notification:read）

- 支持（需要 `notification:read`，仅个人 PAT，组织 Key 禁止访问个人通知）：
  - `GET /notifications`、`GET /notifications/unread-count`
  - `POST /notifications/read`、`POST /notifications/mark-all-read`
  - `DELETE /notifications/{uuid}`

### 管理员端（/admin/*）

- 仅 `ROLE_ADMIN` 可访问，API Key 不具备该角色，默认不可通过 API Key 调用。

## 不支持通过 API Key 的端点

- 用户自我信息：`GET/PATCH /users/me`、`/users/me/stats`、`/users/me/contributions`
- 创建组织：`POST /orgs`（组织 Key 禁止；个人 PAT 需 `hasAnyRole('USER','ADMIN')`，可用 JWT）
- 公开申请/邀请码加入：`POST /orgs/{uuid}/join-requests`、`POST /orgs/invite-links/apply`（组织 Key 禁止）
- API Key 管理：`/api-keys/*`（仅 JWT 登录用户；`hasAnyRole('USER','ADMIN')`）

## 示例

### 使用个人 PAT 读取漏洞分页

```bash
curl -H "X-API-Key: cosv_abcdEF12_secretXXXX" \
  'http://localhost:8080/vulns/search?q=CVE-2025-&page=1&size=20&withTotal=true'
```

### 使用组织 Key 读取本组织漏洞

```bash
curl -H "X-API-Key: cosv_ORGxxxx_secretYYYY" \
  'http://localhost:8080/vulns?organizationUuid=<该Key绑定的组织UUID>&page=1&size=20&withTotal=true'
```

> 若传入其他组织的 `organizationUuid` 将被拒绝。

### 组织 Key 创建漏洞（写入本组织）

```bash
curl -X POST -H "X-API-Key: cosv_ORGxxxx_secretYYYY" -H 'Content-Type: application/json' \
  -d '{
    "organizationUuid": "<该Key绑定的组织UUID>",
    "summary": "漏洞摘要",
    "details": "Markdown detail",
    "severityNum": 6.8,
    "language": "PYTHON",
    "categoryCode": "HTTP_SMUGGLING"
  }' \
  'http://localhost:8080/vulns'
```

### 组织 Key 访问个人通知（将被拒绝）

```bash
curl -H "X-API-Key: cosv_ORGxxxx_secretYYYY" \
  'http://localhost:8080/notifications/unread-count'
# => 组织Key无权访问个人通知
```

## 备注

- 组织级 Key 的范围强校验在服务层完成（例如 `enforceOrgScopeUuid`、`ensureMember/ensureAdmin`），即便具备 `SCOPE_org:read` 也必须满足组织/成员/管理员条件。
- 若要让 API Key 访问更多端点，请在对应控制器上添加合适的 `@PreAuthorize`（以 `SCOPE_...` 为主），并在服务层做好范围与角色校验。

