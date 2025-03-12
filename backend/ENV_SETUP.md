# 环境变量配置指南

为了确保应用程序正常运行且保护敏感信息，请按照以下步骤配置环境变量：

## 本地开发环境

### 方法一：创建 application.yml（推荐）

1. 复制 `src/main/resources/application.yml.template` 到同目录下并重命名为 `application.yml`
2. 在 `application.yml` 中填入实际的配置值
3. 此文件已被添加到 `.gitignore`，不会被提交到代码仓库

### 方法二：设置环境变量

可以在开发环境中设置以下环境变量：

#### 数据库配置
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cosv_horizon
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

#### Redis配置
```
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

#### 邮件配置
```
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password
```

#### JWT配置
```
JWT_SECRET=your_secret_key_here
```

#### 阿里云OSS配置
```
OSS_ENDPOINT=https://oss-cn-beijing.aliyuncs.com
OSS_ACCESS_KEY=your_access_key
OSS_ACCESS_SECRET=your_access_secret
OSS_BUCKET_NAME=your_bucket_name
```

## 生产环境

在生产环境中，强烈建议使用环境变量或外部配置系统来管理敏感信息。以下是常见的处理方式：

### Docker环境

使用 docker-compose 时，可在 docker-compose.yml 的 environment 部分配置环境变量：

```yaml
services:
  app:
    image: cosv-horizon:latest
    environment:
      - DB_HOST=db
      - DB_PORT=3306
      - DB_NAME=cosv_horizon
      - DB_USERNAME=production_user
      - DB_PASSWORD=production_password
      # 其他环境变量
```

### 服务器环境

在服务器上，可以通过以下方式设置环境变量：

1. 将环境变量添加到 `/etc/environment`
2. 在应用程序的启动脚本中设置
3. 使用 systemd service 文件中的 Environment 或 EnvironmentFile 指令

示例 systemd service 文件：
```
[Unit]
Description=COSV-Horizon Service

[Service]
User=app
WorkingDirectory=/opt/cosv-horizon
ExecStart=/usr/bin/java -jar app.jar
EnvironmentFile=/opt/cosv-horizon/config/app.env
Restart=always

[Install]
WantedBy=multi-user.target
```

## 安全建议

1. 使用强密码和密钥
2. 定期轮换密钥和密码
3. 限制数据库用户权限
4. 生产环境中使用加密通信 