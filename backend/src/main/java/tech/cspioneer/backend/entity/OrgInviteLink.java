package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织邀请链接实体")
public class OrgInviteLink {
    private Long id;
    private String uuid;
    private Long orgId;
    private String code;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Boolean isActive;
}

