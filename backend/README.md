# COSV-Horizon 后端开发指南

## 目录结构

COSV-Horizon后端采用标准的Spring Boot项目结构，遵循领域驱动设计(DDD)思想进行组织：

```
src/main/java/com/cosv/horizon/
├── Application.java                # 应用程序入口类
├── config/                         # 配置类目录
│   ├── SecurityConfig.java         # 安全配置
│   ├── RedisConfig.java            # Redis配置
│   └── WebConfig.java              # Web相关配置
├── controller/                     # 控制器层
│   ├── UserController.java         # 用户相关接口
│   ├── OrganizationController.java # 组织相关接口
│   └── VulnerabilityController.java # 漏洞数据相关接口
├── dto/                            # 数据传输对象
│   ├── request/                    # 请求对象
│   └── response/                   # 响应对象
├── entity/                         # 实体类（数据库映射对象）
│   ├── User.java                   # 用户实体
│   ├── Organization.java           # 组织实体
│   └── Vulnerability.java          # 漏洞数据实体
├── mapper/                         # MyBatis映射接口
│   ├── UserMapper.java             # 用户数据操作
│   ├── OrganizationMapper.java     # 组织数据操作
│   └── VulnerabilityMapper.java    # 漏洞数据操作
├── service/                        # 服务接口
│   ├── UserService.java            # 用户服务接口
│   ├── OrganizationService.java    # 组织服务接口
│   └── VulnerabilityService.java   # 漏洞服务接口
├── service/impl/                   # 服务实现类
│   ├── UserServiceImpl.java        # 用户服务实现
│   ├── OrganizationServiceImpl.java # 组织服务实现
│   └── VulnerabilityServiceImpl.java # 漏洞服务实现
├── util/                           # 工具类
│   ├── JwtUtil.java                # JWT工具
│   └── EmailUtil.java              # 邮件工具
└── exception/                      # 异常处理
    ├── GlobalExceptionHandler.java # 全局异常处理器
    └── ApiException.java           # 自定义API异常
```

## 核心层次说明

### 1. 控制器层 (Controller)

控制器层负责处理HTTP请求，进行参数校验和请求路由：

- 使用`@RestController`标注REST API控制器
- 使用`@RequestMapping`定义API路径
- 返回标准化的响应格式
- 不包含业务逻辑，只负责协调和委派

### 2. 服务层 (Service)

服务层实现核心业务逻辑：

- 接口定义在`service`包中
- 实现类放在`service/impl`包中
- 使用`@Service`注解标注服务类
- 通过依赖注入使用其他服务和数据访问层

### 3. 数据访问层

#### Mapper接口

- 使用`@Mapper`注解标注
- 定义SQL操作方法
- 可以使用注解或XML方式实现SQL映射

#### 实体类 (Entity)

- 使用`@Data`、`@Builder`等Lombok注解简化代码
- 使用JPA注解标注实体关系（如有需要）
- 实体类与数据库表结构一一对应

### 4. DTO (数据传输对象)

- 请求DTO：接收前端请求数据
- 响应DTO：返回给前端的数据格式
- 避免直接暴露实体类，提高安全性

## DTP (数据线程池) 说明

DTP (Dynamic Thread Pool) 是一种动态线程池技术，用于优化系统中的线程资源管理：

### DTP核心特性

1. **动态调整**：根据系统负载动态调整线程池参数
2. **实时监控**：监控线程池运行状态和性能指标
3. **告警机制**：当线程池接近饱和时触发告警
4. **隔离策略**：为不同业务场景提供隔离的线程池

### DTP实现方式

```java
@Configuration
public class ThreadPoolConfig {
    
    @Bean
    public ThreadPoolExecutor userOperationThreadPool() {
        return new ThreadPoolExecutor(
            10,                       // 核心线程数
            20,                       // 最大线程数
            60L, TimeUnit.SECONDS,    // 空闲线程存活时间
            new LinkedBlockingQueue<>(100), // 工作队列
            new ThreadFactoryBuilder().setNameFormat("user-operation-pool-%d").build(), // 线程工厂
            new CallerRunsPolicy()    // 拒绝策略
        );
    }
    
    @Bean
    public ThreadPoolExecutor dataProcessThreadPool() {
        return new ThreadPoolExecutor(
            5,                        // 核心线程数
            10,                       // 最大线程数
            60L, TimeUnit.SECONDS,    // 空闲线程存活时间
            new LinkedBlockingQueue<>(500), // 工作队列
            new ThreadFactoryBuilder().setNameFormat("data-process-pool-%d").build(), // 线程工厂
            new CallerRunsPolicy()    // 拒绝策略
        );
    }
}
```

## MyBatis映射方式对比

MyBatis提供了两种主要的SQL映射方式：注解方式和XML配置文件方式。

### 1. 注解方式

```java
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
    
    @Insert("INSERT INTO user(username, email, password) VALUES(#{username}, #{email}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET username = #{username}, email = #{email} WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(Long id);
}
```

**优点**：
- 简单直观，SQL与Java代码在一起
- 适合简单的CRUD操作
- 开发速度快，无需额外配置文件

**缺点**：
- 复杂SQL语句可读性差
- 动态SQL编写繁琐
- 大型项目中维护困难

### 2. XML配置文件方式

**Mapper接口**:
```java
@Mapper
public interface UserMapper {
    User findById(Long id);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
    List<User> findByCondition(UserQueryDTO query);
}
```

**XML映射文件** (resources/mapper/UserMapper.xml):
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.cosv.horizon.mapper.UserMapper">
    
    <resultMap id="userResultMap" type="com.cosv.horizon.entity.User">
        <id property="id" column="id"/>
        <result property="username" column="username"/>
        <result property="email" column="email"/>
        <result property="password" column="password"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>
    
    <select id="findById" resultMap="userResultMap">
        SELECT * FROM user WHERE id = #{id}
    </select>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user(username, email, password)
        VALUES(#{username}, #{email}, #{password})
    </insert>
    
    <update id="update">
        UPDATE user
        SET username = #{username}, email = #{email}
        WHERE id = #{id}
    </update>
    
    <delete id="deleteById">
        DELETE FROM user WHERE id = #{id}
    </delete>
    
    <select id="findByCondition" resultMap="userResultMap">
        SELECT * FROM user
        <where>
            <if test="username != null and username != ''">
                AND username LIKE CONCAT('%', #{username}, '%')
            </if>
            <if test="email != null and email != ''">
                AND email = #{email}
            </if>
            <if test="startDate != null">
                AND created_at >= #{startDate}
            </if>
            <if test="endDate != null">
                AND created_at &lt;= #{endDate}
            </if>
        </where>
        ORDER BY id DESC
    </select>
</mapper>
```

**优点**：
- 支持复杂SQL语句，可读性强
- 动态SQL功能强大且直观
- SQL与Java代码分离，便于维护
- 适合大型项目和复杂查询

**缺点**：
- 配置较复杂
- 需要管理额外的XML文件
- 接口与XML之间需要保持一致

### 最佳实践建议

1. **混合使用**：
   - 简单CRUD操作使用注解方式
   - 复杂查询、动态SQL使用XML方式

2. **项目规模考虑**：
   - 小型项目可以优先使用注解方式
   - 大型项目建议使用XML方式，便于后期维护

3. **统一风格**：
   - 在一个项目中尽量保持一致的映射风格
   - 避免同一个Mapper中混用两种方式

4. **性能优化**：
   - 合理使用缓存机制
   - 使用分页插件处理大数据量查询 

## 配置管理

为了保护敏感信息（如数据库密码、API密钥等）并便于团队协作，项目采用以下配置管理策略：

### 配置文件

1. **配置模板文件**：
   - `application.yml.template` - 包含所有配置项，但敏感值被替换为占位符
   - 此文件应该提交到代码仓库

2. **本地配置文件**：
   - `application.yml` - 包含实际配置值的本地配置文件
   - 此文件已在 `.gitignore` 中，不会被提交到代码仓库

### 环境变量支持

配置文件支持通过环境变量注入敏感信息，格式为：
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:default_value}
```

这种方式允许：
- 通过环境变量 `DB_PASSWORD` 设置密码
- 如未设置环境变量，则使用 `default_value` 作为默认值

详细的环境变量配置指南请参考 `ENV_SETUP.md` 文件。

### 多环境配置

项目支持多环境配置，通过 Spring Profiles 实现：
- `application.yml` - 基础配置
- `application-dev.yml` - 开发环境配置
- `application-test.yml` - 测试环境配置
- `application-prod.yml` - 生产环境配置

可通过以下方式启动指定环境的配置：
```
java -jar app.jar --spring.profiles.active=dev
```

或设置环境变量：
```
SPRING_PROFILES_ACTIVE=dev
``` 