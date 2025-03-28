package com.cosv.horizon.service;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.mapper.OriginalLoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OriginalLoginService {

    @Autowired
    private OriginalLoginMapper originalLoginMapper;

    /**
     * 根据提供商和提供商ID查找用户
     * @param provider 提供商（如github）
     * @param providerId 提供商ID
     * @return 关联的用户，如不存在则返回null
     */
    public User findUserByProviderAndProviderId(String provider, String providerId) {
        return originalLoginMapper.findUserByProviderAndProviderId(provider, providerId);
    }

    /**
     * 保存原始登录信息
     * @param userId 用户ID
     * @param provider 提供商（如github）
     * @param providerId 提供商ID
     * @param name 提供商账号名称
     */
    @Transactional
    public void saveOriginalLogin(Long userId, String provider, String providerId, String name) {
        originalLoginMapper.insert(userId, provider, providerId, name);
    }
} 