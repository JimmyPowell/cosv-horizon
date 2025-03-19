package com.cosv.horizon.service;

import com.cosv.horizon.entity.request.CodeVerifyRequest;

public interface AuthService {

    boolean generateCode(String email);
    boolean validateCode(CodeVerifyRequest request);

}
