package com.hkd.service;

import com.hkd.pojo.SysUserRoleQuery;
import org.apache.ibatis.annotations.Param;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 19:14
 * @description TODO: 类功能描述
 */
public interface SysUserRoleQueryService {
    //登录验证逻辑业务
    SysUserRoleQuery findSysUserRoleBySysUserId(String username);
    
    //根据用户ID查询用户角色信息
    SysUserRoleQuery findByUserId(Integer userId);
}
