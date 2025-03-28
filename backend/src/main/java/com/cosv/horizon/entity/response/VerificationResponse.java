package com.cosv.horizon.entity.response;

import lombok.Data;

@Data
public class VerificationResponse {
    private String sessionId;
    
    public VerificationResponse(String sessionId) {
        this.sessionId = sessionId;
    }
} 