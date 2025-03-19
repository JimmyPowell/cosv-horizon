package com.cosv.horizon.entity.request;


import lombok.Data;

@Data
public class CodeVerifyRequest {
    private String code;
    private String email;
}
