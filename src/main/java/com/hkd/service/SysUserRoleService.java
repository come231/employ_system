package com.hkd.service;

import com.hkd.pojo.SysUserRole;

import java.util.List;

public interface SysUserRoleService {
    // 根据用户ID查询角色
    SysUserRole findByUserId(Integer userId);
    
    // 根据角色ID查询用户
    List<SysUserRole> findByRoleId(Integer roleId);
    
    // 新增用户角色关联
    int insert(SysUserRole sysUserRole);
    
    // 删除用户角色关联
    int delete(Integer id);
    
    // 根据用户ID和角色ID删除关联
    int deleteByUserIdAndRoleId(Integer userId, Integer roleId);
    
    // 根据用户ID删除所有关联角色
    int deleteByUserId(Integer userId);
    
    // 根据角色ID删除所有关联用户
    int deleteByRoleId(Integer roleId);
    
    // 批量为用户分配角色
    int batchInsert(List<SysUserRole> userRoleList);
    
    // 批量删除用户角色关联
    int batchDelete(List<Integer> idList);

    // 根据用户ID更新角色
    int updateRoleByUserId(Integer userId, Integer roleId);
}