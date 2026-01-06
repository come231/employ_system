package com.hkd.service;

import com.hkd.pojo.SysRole;

import java.util.List;

public interface SysRoleService {
    // 查询所有角色
    List<SysRole> findAll();
    
    // 根据ID查询角色
    SysRole findById(Integer roleId);
    
    // 新增角色
    int insert(SysRole sysRole);
    
    // 更新角色
    int update(SysRole sysRole);
    
    // 删除角色
    int delete(Integer roleId);
    
    // 根据角色类型查询角色
    List<SysRole> findByRoleType(Byte roleType);
    
    // 根据状态查询角色
    List<SysRole> findByStatus(Byte status);

    // 根据角色名称查询角色
    SysRole findByRoleName(String roleName);
}