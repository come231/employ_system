package com.hkd.mapper;

import com.hkd.pojo.SysUserRoleQuery;
import org.apache.ibatis.annotations.Param;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 19:00
 * @description TODO: 进行登录验证，返回相应的登录信息
 */
public interface SysUserRoleQueryDao {
    //登录验证参数为角色类型，账号，密码
    SysUserRoleQuery findSysUserRoleBySysUserId(String username);
    
    //根据用户ID查询用户角色信息
    SysUserRoleQuery findByUserId(@Param("userId") Integer userId);
}
