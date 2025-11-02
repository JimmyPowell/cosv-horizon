package tech.cspioneer.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentView {
    private String commentUuid;
    private String content;
    private Boolean isEdited;
    private String status;
    private LocalDateTime createTime;
    private String userUuid;
    private String userName;
    private String userAvatar;
}

