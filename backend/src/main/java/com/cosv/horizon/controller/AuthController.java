package com.cosv.horizon.controller;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.CodeVerifyRequest;
import com.cosv.horizon.entity.request.GenerateCodeRequest;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.cosv.horizon.utils.Constans;
@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 发送验证码到用户邮箱中，同时将对应的邮箱-验证码键值对存入redis
     * @param request 包含email字段的请求体
     * @return 0-成功 1-失败
     */
    @PostMapping("generatecode")
    public ResponseEntity<HttpResponseEntity> generateCode(@RequestBody GenerateCodeRequest request) {
        boolean result = authService.generateCode(request.getEmail());
        if (result) {
            return ResponseEntity.ok(new HttpResponseEntity(Constans.SUCCESS_CODE, Constans.SUCCESS_MESSAGE, "验证码发送成功"));
        } else {
            return ResponseEntity.ok(new HttpResponseEntity(Constans.EXIST_CODE , Constans.EXIST_MESSAGE, "验证码发送失败"));
        }
    }

    @PostMapping("verifycode") 
    public ResponseEntity<HttpResponseEntity> verifyCode(@RequestBody CodeVerifyRequest codeVerifyRequest) {
        boolean result = authService.validateCode(codeVerifyRequest);
        if (result) {
            return ResponseEntity.ok(new HttpResponseEntity(Constans.SUCCESS_CODE, Constans.SUCCESS_MESSAGE, "验证码验证成功"));
        } else {
            return ResponseEntity.ok(new HttpResponseEntity(Constans.EXIST_CODE , Constans.EXIST_MESSAGE, "验证码验证失败"));
        }
    }

    @PostMapping("register")
    public int register(@RequestBody User user) {
        //TODO:完成注册的逻辑
        return 1;
    }
}

