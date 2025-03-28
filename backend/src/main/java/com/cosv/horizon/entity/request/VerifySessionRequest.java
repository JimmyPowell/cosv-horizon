package com.cosv.horizon.entity.request;

import lombok.Data;

@Data
public class VerifySessionRequest {
    private String sessionId;
    private String code;
} 