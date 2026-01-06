package com.hkd.util;

import java.util.UUID;

/**
 * Token工具类
 * 用于生成和验证Token（简化版，实际项目中应使用JWT）
 */
public class TokenUtil {
    
    /**
     * 生成Token
     * 实际项目中应该使用JWT生成包含用户信息的Token
     */
    public static String generateToken(Integer userId) {
        // 简化处理：使用UUID + 用户ID
        // 实际项目中应该使用JWT，包含用户ID、过期时间等信息
        return UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }
    
    /**
     * 从Token中提取用户ID
     * 实际项目中应该解析JWT Token
     */
    public static Long getUserIdFromToken(String token) {
        try {
            if (CommonUtil.isEmpty(token)) {
                return null;
            }
            
            // 简化处理：从token中提取用户ID
            // 实际项目中应该解析JWT Token
            if (token.contains("_")) {
                String userIdStr = token.substring(token.lastIndexOf("_") + 1);
                return Long.parseLong(userIdStr);
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     * 实际项目中应该：
     * 1. 验证JWT签名
     * 2. 检查Token是否过期
     * 3. 检查Token是否在黑名单中（已登出）
     */
    public static boolean validateToken(String token) {
        if (CommonUtil.isEmpty(token)) {
            return false;
        }
        
        // 简化处理：检查格式
        // 实际项目中应该验证JWT签名和过期时间
        return token.contains("_") && getUserIdFromToken(token) != null;
    }
}


