package com.cosv.horizon.entity.request;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String sessionId;
    private String name;
    private String password;
    private String role;
    private String avatar;
    private String company;
    private String location;
    private String gitHub;
    private String website;
    private String freeText;
    private String realName;
} 