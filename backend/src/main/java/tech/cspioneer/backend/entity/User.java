package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.UserStatus;
import tech.cspioneer.backend.enums.UserRole;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class User {

    @Schema(description = "用户ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "用户全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440001")
    private String uuid;

    @Schema(description = "用户名称，不可为空且唯一", example = "admin")
    private String name;

    @Schema(description = "用户密码", example = "encrypted_password")
    private String password;

    @Schema(description = "用户全局角色", example = "ADMIN")
    private UserRole role;

    @Schema(description = "用户电子邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "用户头像路径", example = "/avatars/admin.jpg")
    private String avatar;

    @Schema(description = "用户所属公司", example = "COSV Horizon")
    private String company;

    @Schema(description = "用户所在地", example = "Beijing, China")
    private String location;

    @Schema(description = "GitHub链接", example = "https://github.com/username")
    private String gitHub;

    @Schema(description = "用户状态", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "用户评分", example = "100")
    private Long rating;

    @Schema(description = "用户个人网站", example = "https://example.com")
    private String website;

    @Schema(description = "用户自由描述文本", example = "Security researcher")
    private String freeText;

    @Schema(description = "用户真实姓名", example = "John Doe")
    private String realName;

    @Schema(description = "创建日期", example = "2023-01-01T00:00:00")
    private LocalDateTime createDate;

    @Schema(description = "更新日期", example = "2023-01-01T00:00:00")
    private LocalDateTime updateDate;
}
