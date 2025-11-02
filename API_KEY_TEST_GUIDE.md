# API Key 端点测试指南（含 COSV 文件三步导入）

本文档说明如何用 API Key 访问后端端点，重点覆盖“通过文件提交漏洞”的三步流程（上传原文 → 预检解析 → 确认入库）。同时给出个人 Key 与组织级 Key 的测试方法、所需权限、常见问题排查。

> 约定：以下命令示例在 macOS/Linux 终端，需安装 curl 和 jq。

## 准备工作

- 后端已运行：`http://localhost:8080`
- 你已拥有一个可登录的账号（用于创建 API Key）
- 可选：示例 COSV JSON 文件（已提供）：`docs/examples/cosv_sample_full.json`

设置环境变量（根据实际替换）：

```bash
export BASE="http://localhost:8080"
export EMAIL="<你的登录邮箱或用户名>"
export PASSWORD="<你的登录密码>"
```

登录并获取 Bearer Token（仅用于创建/管理 API Key，不用于业务测试）：

```bash
LOGIN_JSON=$(curl -sS -H 'Content-Type: application/json' \
  -X POST "$BASE/auth/login" \
  -d '{"login":"'"$EMAIL"'","password":"'"$PASSWORD"'"}')
ACC=$(echo "$LOGIN_JSON" | jq -r '.data.accessToken')
USER_UUID=$(echo "$LOGIN_JSON" | jq -r '.data.user.uuid')
```

## 关于 API Key 的作用与权限

- 认证头两种写法，二选一：
  - `X-API-Key: <cosv_<prefix>_<secret>>`
  - `Authorization: ApiKey <cosv_<prefix>_<secret>>`
- API Key 携带的权限（scopes）决定可访问的端点能力：
  - 只读：`vuln:read`
  - 创建/导入：`vuln:write`
  - 组织相关：`org:read`, `org:write`（仅组织级 Key 允许）
- 区分主体类型：
  - 个人 Key（USER）：不可代表组织提交（不得在请求里带 `organizationUuid`）。
  - 组织级 Key（ORG）：默认绑定某个组织；以组织身份提交时，若请求里不传 `organizationUuid`，后端会自动使用 Key 所属组织。
- 组织权限：
  - 文件导入三步（`/cosv/**`）以组织身份时，当前用户需是该组织 ADMIN（管理员）。
  - 表单提交（`POST /vulns`）以组织身份时，成员（MEMBER）即可，但组织必须处于 ACTIVE 状态。

## 一、创建 API Key（使用 Bearer Token）

创建“个人 Key（USER）”：具有漏洞读写权限（无过期时间）

```bash
curl -sS -H "Authorization: Bearer $ACC" -H 'Content-Type: application/json' \
  -X POST "$BASE/api-keys" \
  -d '{
    "description": "personal test key",
    "scopes": ["vuln:read", "vuln:write"]
  }' | tee /tmp/apikey-create.json | jq

APIKEY=$(jq -r '.data.apiKey' </tmp/apikey-create.json)
AK_UUID=$(jq -r '.data.uuid' </tmp/apikey-create.json)
```

创建“组织级 Key（ORG）”：绑定组织，具备漏洞写与组织读权限（需要你是该组织 ADMIN）

```bash
export ORG_UUID="<你的组织 UUID，且你是 ADMIN>"

curl -sS -H "Authorization: Bearer $ACC" -H 'Content-Type: application/json' \
  -X POST "$BASE/api-keys" \
  -d '{
    "organizationUuid": "'"$ORG_UUID"'",
    "description": "org test key",
    "scopes": ["vuln:write", "org:read"]
  }' | tee /tmp/apikey-org-create.json | jq

ORG_APIKEY=$(jq -r '.data.apiKey' </tmp/apikey-org-create.json)
ORG_AK_UUID=$(jq -r '.data.uuid' </tmp/apikey-org-create.json)
```

> 过期时间（可选）：支持 `Z`（UTC）或带时区偏移的 ISO-8601，或本地时间 `yyyy-MM-dd HH:mm:ss`，且必须晚于“现在”。
>
> 例如：`"expireTime": "2025-12-31T10:00:00Z"`

## 二、基本可用性验证（API Key）

用“个人 Key（USER）”做只读查询：

```bash
curl -sS -H "X-API-Key: $APIKEY" "$BASE/vulns/search?size=1&withTotal=true" | jq
```

返回 `{code:0}` 即代表成功；若 `401` 检查 Key 是否过期或格式是否 `cosv_<prefix>_<secret>`。

## 三、COSV 文件导入（个人 Key）

以个人 Key（USER）完成 “上传 → 预检 → 入库” 串联（不可代表组织提交）：

1) 上传原文

```bash
RAW=$(curl -sS -H "X-API-Key: $APIKEY" \
  -F "file=@docs/examples/cosv_sample_full.json" \
  "$BASE/cosv/files" | jq -r '.data.rawFileUuid')
echo "RAW_FILE_UUID=$RAW"
```

2) 预检解析（批量自动识别；也可用 `/parse` 单条接口）

```bash
curl -sS -H "X-API-Key: $APIKEY" -X POST \
  "$BASE/cosv/files/$RAW/parse-batch?language=JAVA&categoryCode=SEC_TEST&tagCodes=RCE,XSS&mode=AUTO" | jq
```

3) 确认入库（单条）

```bash
curl -sS -H "X-API-Key: $APIKEY" -H 'Content-Type: application/json' \
  -X POST "$BASE/cosv/files/$RAW/ingest" \
  -d '{
    "action": "CREATE",
    "conflictPolicy": "FAIL",
    "language": "JAVA",
    "categoryCode": "SEC_TEST",
    "tagCodes": "RCE,XSS"
  }' | jq
```

> 注意：个人 Key 不得在请求体/参数中携带 `organizationUuid`。

## 四、COSV 文件导入（组织级 Key）

使用组织级 Key（ORG）以组织身份提交（需你是该组织 ADMIN，且组织状态 ACTIVE）：

1) 上传原文（组织身份）

```bash
RAW_ORG=$(curl -sS -H "X-API-Key: $ORG_APIKEY" \
  -F "file=@docs/examples/cosv_sample_full.json" \
  -F "organizationUuid=$ORG_UUID" \
  "$BASE/cosv/files" | jq -r '.data.rawFileUuid')
echo "RAW_FILE_UUID_ORG=$RAW_ORG"
```

2) 预检解析

```bash
curl -sS -H "X-API-Key: $ORG_APIKEY" -X POST \
  "$BASE/cosv/files/$RAW_ORG/parse-batch?language=JAVA&categoryCode=SEC_TEST&tagCodes=RCE,XSS&mode=AUTO" | jq
```

3) 确认入库（可省略 organizationUuid；后端会从组织 Key 上下文自动绑定）

```bash
curl -sS -H "X-API-Key: $ORG_APIKEY" -H 'Content-Type: application/json' \
  -X POST "$BASE/cosv/files/$RAW_ORG/ingest" \
  -d '{
    "action": "CREATE",
    "conflictPolicy": "FAIL",
    "language": "JAVA",
    "categoryCode": "SEC_TEST",
    "tagCodes": "RCE,XSS"
  }' | jq
```

> 若传了 `organizationUuid`，必须与组织 Key 绑定组织一致，否则返回 `1012`。

## 五、用 API Key 直接创建漏洞（非文件导入）

- 个人 Key（USER）：允许以个人身份创建（不可带 `organizationUuid`）

```bash
curl -sS -H "X-API-Key: $APIKEY" -H 'Content-Type: application/json' \
  -X POST "$BASE/vulns" \
  -d '{
    "summary": "APIKey 提交示例（个人）",
    "details": "示例详情",
    "severityNum": 6.5,
    "language": "JAVA",
    "categoryCode": "SEC_TEST",
    "tagCodes": ["RCE"]
  }' | jq
```

- 组织级 Key（ORG）：以组织身份创建（可不传 `organizationUuid`，默认使用 Key 所属组织）

```bash
curl -sS -H "X-API-Key: $ORG_APIKEY" -H 'Content-Type: application/json' \
  -X POST "$BASE/vulns" \
  -d '{
    "summary": "APIKey 提交示例（组织）",
    "details": "示例详情",
    "severityNum": 7.1,
    "language": "JAVA",
    "categoryCode": "SEC_TEST",
    "tagCodes": ["RCE"]
  }' | jq
```

## 六、常见问题排查（FAQ）

- 401 Unauthorized（HTTP）：
  - API Key 过期：创建时 `expireTime` 必须是将来时间；支持 `Z`/带时区/本地格式。
  - Key 格式错误：必须是 `cosv_<prefix>_<secret>`。
  - 未包含所需 scope：例如 `/cosv/**` 需要 `vuln:write`。
- 403 无权限（code=403）：
  - 组织权限不足：上传/入库以组织身份时，需你是该组织 ADMIN；表单创建以组织身份时，组织需 ACTIVE。
  - 个人 Key 代表组织提交：不允许；用组织级 Key 或按个人身份提交。
- 500 服务器内部错误：
  - 若发生，请检查后端 `backend.log`；通常应返回 `401/403` 而非 `500`，如遇异常可反馈日志定位。
- 为什么用 API Key 不能管理 API Key 自身（创建/列表/用量）？
  - 这些端点是用户自服务管理，要求用户登录态（Bearer）；API Key 仅用于业务访问。

## 七、验证 API Key 使用日志（可选）

- 该端点需要 Bearer（登录态）且你对目标 Key 有权限：

```bash
curl -sS -H "Authorization: Bearer $ACC" \
  "$BASE/api-keys/$AK_UUID/usage?size=5" | jq
```

若成功可看到近期使用记录（方法、路径、状态码、时间戳等）。

---

如需将上述步骤做成一键脚本（同时展示个人 Key 与组织 Key 的完整导入流程），我可以继续在 `scripts/` 下提供可执行脚本并内置校验与错误提示。 
