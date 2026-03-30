package com.hkd.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TokenUtil {

    @Autowired
    private RedisUtil redisUtil;

    // 官方安全密钥 → 解决所有 base64 / 密钥过短错误
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // Token前缀
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    // 过期时间 30 分钟
    private static final long EXPIRATION = 1000 * 60 * 30;

    /**
     * 生成访问令牌（30分钟）
     */
    // 生成 Token
    /**
     * 生成 Token（传入 userId）
     */
    public  String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // ✅ 这里把 userId 放进去

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 生成刷新令牌（7天）(存储在redis中)
     */
    public  String generateRefreshToken(Integer userId) {
        return "rtk_" + UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }

    // 解析 Token
    public static Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    // 校验 Token 是否有效
    public boolean validateToken(String token) {
        return getClaims(token) != null;
    }


    /*
    根据刷新令牌刷新Token
     */
    public String refresh(String refreshToken){

        // 1. 判断 refreshToken 是否为空
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }

        // 2. 根据 RT 反查 userId（你必须把 RT 和 userId 绑定存储）
        // 优化方案：Redis 存 key=refresh_token:{RT}  value=userId
        String userId = (String) redisUtil.get(refreshToken);
        if (userId == null) {
            return null;
        }

        // 3. 生成新 AccessToken
        String newAccessToken = generateToken(userId);

        // 4. 返回新 AT
        return newAccessToken;

    }


}