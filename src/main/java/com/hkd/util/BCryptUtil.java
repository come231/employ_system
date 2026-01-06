package com.hkd.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * BCrypt密码加密工具类
 */
public class BCryptUtil {
    
    /**
     * 加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * 验证密码
     * @param password 明文密码
     * @param hashedPassword 加密后的密码
     * @return 验证通过返回true，否则返回false
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成BCrypt加密的密码（用于初始化数据）
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String generateHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     * 获得16个长度的十六进制的UUID
     *
     * @return UUID
     */
    public static String get16UUID() {
        UUID id = UUID.randomUUID();
        String[] idd = id.toString().split("-");
        return idd[0] + idd[1] + idd[2];
    }
}






