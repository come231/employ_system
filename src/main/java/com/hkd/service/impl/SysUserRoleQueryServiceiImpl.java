package com.hkd.service.impl;

import com.hkd.mapper.SysUserRoleQueryDao;
import com.hkd.pojo.SysUserRoleQuery;
import com.hkd.service.SysUserRoleQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 19:18
 * @description TODO: 类功能描述
 */
@Service
public class SysUserRoleQueryServiceiImpl implements SysUserRoleQueryService {
    @Autowired
    SysUserRoleQueryDao sysUserRoleQueryDao;

    //进行登录验证
    @Override
    public SysUserRoleQuery findSysUserRoleBySysUserId(String username) {
        return sysUserRoleQueryDao.findSysUserRoleBySysUserId(username);
    }

    //根据用户ID查询用户角色信息
    @Override
    public SysUserRoleQuery findByUserId(Integer userId) {
        return sysUserRoleQueryDao.findByUserId(userId);
    }
}
